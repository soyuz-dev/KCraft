package org.soyuz.kcraft.golem

import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController
import com.geckolib.animation.RawAnimation
import com.geckolib.util.GeckoLibUtil
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.UUID

class RubyGolem(
    type: EntityType<out RubyGolem>,
    level: Level
) : PathfinderMob(
    type,
    level
), GeoEntity {


    override fun addAdditionalSaveData(
        output: ValueOutput
    ) {
        super.addAdditionalSaveData(output)

        parentComputerId?.let {
            output.putString(
                "ParentComputer",
                it.toString()
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

    override fun registerGoals() {
        // Intentionally empty.
        //
        // Ruby Golems are controlled by KCraft computers,
        // not autonomous Minecraft AI.
    }

    companion object {

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

    private fun findInterestingEntity(): LivingEntity? {
        val nearby =
            level().getEntitiesOfClass(
                LivingEntity::class.java,
                boundingBox.inflate(8.0)
            ) { entity ->
                entity !== this &&
                        entity.isAlive
            }

        return nearby.randomOrNull()
    }

    private fun lookAnglesTo(
        target: LivingEntity
    ): Pair<Double, Double> {
        val from =
            eyePosition

        val to =
            target.eyePosition

        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z

        val horizontalDistance =
            kotlin.math.sqrt(
                dx * dx + dz * dz
            )

        val worldYaw =
            Math.toDegrees(
                kotlin.math.atan2(
                    dz,
                    dx
                )
            ) - 90.0

        val yaw =
            Mth.wrapDegrees(
                worldYaw - yBodyRot
            )

        val pitch =
            -Math.toDegrees(
                kotlin.math.atan2(
                    dy,
                    horizontalDistance
                )
            )

        return yaw to pitch
    }

    private fun chooseLookTarget(): Boolean {
        val target =
            findInterestingEntity()
                ?: return false

        val (yaw, pitch) =
            lookAnglesTo(target)

        if (kotlin.math.abs(yaw) > 60.0) {
            return false
        }

        lookYaw =
            yaw.coerceIn(
                -45.0,
                45.0
            )

        lookPitch =
            pitch.coerceIn(
                -25.0,
                25.0
            )

        return true
    }

    var lookYaw = 0.0
        private set

    var lookPitch = 0.0
        private set

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
                "idle",
                4
            ) { test ->

                val controller =
                    test.controller()

                if (controller.hasAnimationFinished()) {
                    idleAnimation =
                        when (idleAnimation) {
                            IdleAnimation.STATIONARY -> {
                                if (
                                    random.nextFloat() <= 0.4f &&
                                    chooseLookTarget()
                                ) {
                                    IdleAnimation.LOOK_AT
                                } else {
                                    IdleAnimation.STATIONARY
                                }
                            }

                            IdleAnimation.LOOK_AT ->
                                IdleAnimation.STATIONARY
                        }
                }

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
        )
    }

    private enum class IdleAnimation {
        STATIONARY,
        LOOK_AT
    }

    private var idleAnimation =
        IdleAnimation.STATIONARY
}