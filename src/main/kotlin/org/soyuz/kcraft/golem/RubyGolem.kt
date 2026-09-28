package org.soyuz.kcraft.golem

import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.util.GeckoLibUtil
import net.minecraft.world.entity.EntityType
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

    private val geoCache =
        GeckoLibUtil.createInstanceCache(this)

    override fun getAnimatableInstanceCache():
            AnimatableInstanceCache =
        geoCache

    override fun registerControllers(
        controllers: AnimatableManager.ControllerRegistrar
    ) {
        // Nothing yet
    }
}