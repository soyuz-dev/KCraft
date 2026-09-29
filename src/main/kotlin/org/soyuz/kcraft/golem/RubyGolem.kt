package org.soyuz.kcraft.golem

import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController
import com.geckolib.animation.RawAnimation
import com.geckolib.util.GeckoLibUtil
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


class RubyGolem(
    type: EntityType<out RubyGolem>,
    level: Level
) : PathfinderMob(
    type,
    level
), GeoEntity {

    // -------------------------------------------------------------------------
    // Computer ownership
    // -------------------------------------------------------------------------

    var parentComputerId: UUID? = null
        private set

    fun bindToComputer(
        computerId: UUID
    ) {
        check(parentComputerId == null) {
            "Ruby Golem is already bound to a computer"
        }

        parentComputerId = computerId

        println(
            "Ruby Golem $uuid bound to computer $computerId"
        )
    }


    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    override fun addAdditionalSaveData(
        output: ValueOutput
    ) {
        super.addAdditionalSaveData(output)

        parentComputerId?.let { computerId ->
            output.putString(
                "ParentComputer",
                computerId.toString()
            )
        }
    }

    override fun readAdditionalSaveData(
        input: ValueInput
    ) {
        super.readAdditionalSaveData(input)

        parentComputerId =
            input.getString("ParentComputer")
                .map(UUID::fromString)
                .orElse(null)
    }


    // -------------------------------------------------------------------------
    // Minecraft AI
    // -------------------------------------------------------------------------

    override fun registerGoals() {
        // Intentionally empty for now.
        //
        // Ruby Golems do not autonomously perform jobs.
        // Computer-controlled behaviour and small Allay-like instincts
        // will be added deliberately.
    }


    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    /**
     * Temporary development hook.
     *
     * This will eventually be replaced by proper KCraft golem commands.
     */
    fun debugMoveTo(
        x: Double,
        y: Double,
        z: Double
    ) {
        navigation.moveTo(
            x,
            y,
            z,
            0.5
        )
    }

    private val moving: Boolean
        get() =
            deltaMovement.horizontalDistanceSqr() >
                    MOVEMENT_THRESHOLD_SQUARED


    // -------------------------------------------------------------------------
    // Look-at behaviour
    // -------------------------------------------------------------------------

    var lookYaw = 0.0
        private set

    var lookPitch = 0.0
        private set

    private fun findInterestingEntity(): LivingEntity? {
        val nearby =
            level().getEntitiesOfClass(
                LivingEntity::class.java,
                boundingBox.inflate(
                    INTEREST_RADIUS
                )
            ) { entity ->
                entity !== this &&
                        entity.isAlive
            }

        return nearby.randomOrNull()
    }

    private fun chooseLookTarget(): Boolean {
        val target =
            findInterestingEntity()
                ?: return false

        val (yaw, pitch) =
            lookAnglesTo(target)

        // Don't mysteriously stare exactly 45 degrees sideways
        // because something interesting is actually behind us.
        if (abs(yaw) > MAX_TARGET_YAW) {
            return false
        }

        lookYaw =
            yaw.coerceIn(
                -MAX_LOOK_YAW,
                MAX_LOOK_YAW
            )

        lookPitch =
            pitch.coerceIn(
                -MAX_LOOK_PITCH,
                MAX_LOOK_PITCH
            )

        return true
    }

    private fun lookAnglesTo(
        target: LivingEntity
    ): Pair<Double, Double> {
        val from =
            eyePosition

        val to =
            target.eyePosition

        val dx =
            to.x - from.x

        val dy =
            to.y - from.y

        val dz =
            to.z - from.z

        val horizontalDistance =
            sqrt(
                dx * dx +
                        dz * dz
            )

        val worldYaw =
            Math.toDegrees(
                atan2(
                    dz,
                    dx
                )
            ) - 90.0

        val yaw =
            Mth.wrapDegrees(
                worldYaw -
                        yBodyRot
            )

        val pitch =
            -Math.toDegrees(
                atan2(
                    dy,
                    horizontalDistance
                )
            )

        return yaw to pitch
    }


    // -------------------------------------------------------------------------
    // GeckoLib
    // -------------------------------------------------------------------------

    private val geoCache =
        GeckoLibUtil.createInstanceCache(this)

    override fun getAnimatableInstanceCache():
            AnimatableInstanceCache =
        geoCache

    override fun registerControllers(
        controllers: AnimatableManager.ControllerRegistrar
    ) {
        controllers.add(
            AnimationController<RubyGolem>(
                "main",
                ANIMATION_TRANSITION_TICKS
            ) { test ->
                val controller =
                    test.controller()

                updateLocomotionAnimation(
                    controller
                )

                when (locomotionAnimation) {
                    LocomotionAnimation.IDLE -> {
                        updateIdleAnimation(
                            controller
                        )

                        when (idleAnimation) {
                            IdleAnimation.STATIONARY ->
                                test.setAndContinue(
                                    IDLE_STATIONARY
                                )

                            IdleAnimation.LOOK_AT ->
                                test.setAndContinue(
                                    IDLE_LOOK_AT
                                )
                        }
                    }

                    LocomotionAnimation.STARTING ->
                        test.setAndContinue(
                            WALK_START
                        )

                    LocomotionAnimation.WALKING ->
                        test.setAndContinue(
                            WALK_CONTINUOUS
                        )

                    LocomotionAnimation.STOPPING ->
                        test.setAndContinue(
                            WALK_END
                        )
                }
            }
        )
    }


    // -------------------------------------------------------------------------
    // Animation state
    // -------------------------------------------------------------------------

    private enum class IdleAnimation {
        STATIONARY,
        LOOK_AT
    }

    private enum class LocomotionAnimation {
        IDLE,
        STARTING,
        WALKING,
        STOPPING
    }

    private var idleAnimation =
        IdleAnimation.STATIONARY

    private var locomotionAnimation =
        LocomotionAnimation.IDLE


    // -------------------------------------------------------------------------
    // Idle animation state machine
    // -------------------------------------------------------------------------

    private fun updateIdleAnimation(
        controller: AnimationController<RubyGolem>
    ) {
        if (!controller.hasAnimationFinished()) {
            return
        }

        idleAnimation =
            when (idleAnimation) {
                IdleAnimation.STATIONARY -> {
                    if (
                        random.nextFloat() <=
                        LOOK_AT_CHANCE &&
                        chooseLookTarget()
                    ) {
                        IdleAnimation.LOOK_AT
                    } else {
                        // We are asking GeckoLib to play the same
                        // non-looping animation again.
                        controller.reset()

                        IdleAnimation.STATIONARY
                    }
                }

                IdleAnimation.LOOK_AT ->
                    IdleAnimation.STATIONARY
            }
    }


    // -------------------------------------------------------------------------
    // Locomotion animation state machine
    // -------------------------------------------------------------------------

    private fun updateLocomotionAnimation(
        controller: AnimationController<RubyGolem>
    ) {
        locomotionAnimation =
            when (locomotionAnimation) {
                LocomotionAnimation.IDLE -> {
                    if (moving) {
                        // If movement interrupts a look-at animation,
                        // forget that idle sub-state. When we next stop,
                        // we begin from ordinary stationary idle.
                        idleAnimation =
                            IdleAnimation.STATIONARY

                        LocomotionAnimation.STARTING
                    } else {
                        LocomotionAnimation.IDLE
                    }
                }

                LocomotionAnimation.STARTING -> {
                    when {
                        !moving ->
                            LocomotionAnimation.STOPPING

                        controller.hasAnimationFinished() ->
                            LocomotionAnimation.WALKING

                        else ->
                            LocomotionAnimation.STARTING
                    }
                }

                LocomotionAnimation.WALKING -> {
                    if (moving) {
                        LocomotionAnimation.WALKING
                    } else {
                        LocomotionAnimation.STOPPING
                    }
                }

                LocomotionAnimation.STOPPING -> {
                    when {
                        // Gerald has received another reason to move
                        // before his stopping animation finished.
                        moving ->
                            LocomotionAnimation.STARTING

                        controller.hasAnimationFinished() -> {
                            idleAnimation =
                                IdleAnimation.STATIONARY

                            // WALK_END has just finished. Clear the
                            // controller before returning to idle so
                            // the finished WALK_END state isn't mistaken
                            // for a finished idle animation below.
                            controller.reset()

                            LocomotionAnimation.IDLE
                        }

                        else ->
                            LocomotionAnimation.STOPPING
                    }
                }
            }
    }


    // -------------------------------------------------------------------------
    // Entity attributes
    // -------------------------------------------------------------------------

    companion object {

        private const val INTEREST_RADIUS =
            8.0

        private const val MAX_TARGET_YAW =
            60.0

        private const val MAX_LOOK_YAW =
            45.0

        private const val MAX_LOOK_PITCH =
            25.0

        private const val LOOK_AT_CHANCE =
            0.8f

        private const val MOVEMENT_THRESHOLD_SQUARED =
            0.0001

        private const val ANIMATION_TRANSITION_TICKS =
            4


        // ---------------------------------------------------------------------
        // Animations
        // ---------------------------------------------------------------------

        private val IDLE_STATIONARY =
            RawAnimation.begin()
                .thenPlay(
                    "animation.ruby_golem.idle.stationary"
                )

        private val IDLE_LOOK_AT =
            RawAnimation.begin()
                .thenPlay(
                    "animation.ruby_golem.idle.look_at"
                )

        private val WALK_START =
            RawAnimation.begin()
                .thenPlay(
                    "animation.ruby_golem.walk.start"
                )

        private val WALK_CONTINUOUS =
            RawAnimation.begin()
                .thenLoop(
                    "animation.ruby_golem.walk.continuous"
                )

        private val WALK_END =
            RawAnimation.begin()
                .thenPlay(
                    "animation.ruby_golem.walk.end"
                )


        // ---------------------------------------------------------------------
        // Attributes
        // ---------------------------------------------------------------------

        fun createAttributes() =
            createMobAttributes()
                .add(
                    Attributes.MAX_HEALTH,
                    20.0
                )
                .add(
                    Attributes.MOVEMENT_SPEED,
                    0.25
                )
    }

    override fun mobInteract(
        player: Player,
        hand: InteractionHand
    ): InteractionResult {
        if (!level().isClientSide) {
            val direction =
                lookAngle.multiply(
                    5.0,
                    0.0,
                    5.0
                )

            debugMoveTo(
                x + direction.x,
                y,
                z + direction.z
            )
        }

        return InteractionResult.SUCCESS
    }
}