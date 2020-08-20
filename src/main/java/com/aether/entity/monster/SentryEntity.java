package com.aether.entity.monster;

import com.aether.block.AetherBlocks;
import com.aether.entity.AetherEntityTypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class SentryEntity extends SlimeEntity {
	public static final DataParameter<Boolean> SENTRY_AWAKE = EntityDataManager.createKey(SentryEntity.class, DataSerializers.BOOLEAN);
	
	public float timeSpotted = 0.0f;
	
	public SentryEntity(EntityType<? extends SentryEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	public SentryEntity(World worldIn) {
		super(AetherEntityTypes.SENTRY, worldIn);
	}
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new SentryEntity.FloatGoal(this));
		this.goalSelector.addGoal(2, new SentryEntity.AttackGoal(this));
		this.goalSelector.addGoal(3, new SentryEntity.FaceRandomGoal(this));
		this.goalSelector.addGoal(5, new SentryEntity.HopGoal(this));
		this.targetSelector.addGoal(1,
			new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, (p_213811_1_) -> {
				return Math.abs(p_213811_1_.getPosY() - this.getPosY()) <= 4.0D;
			}));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(SENTRY_AWAKE, false);
	}
	
	@Override
	public void onCollideWithPlayer(PlayerEntity entityIn) {
		if (EntityPredicates.CAN_AI_TARGET.test(entityIn)) {
			this.explodeAt(entityIn);
		}
	}
	
	@Override
	public void tick() {
		if (this.world.getClosestPlayer(this.getPosX(), this.getPosY(), this.getPosZ(), 8.0, EntityPredicates.CAN_AI_TARGET) != null) {
			if (!this.isAwake()) {
				if (this.timeSpotted >= 24) {
					this.setAwake(true);
				}
				this.timeSpotted++;
			}
		}
		else {
			this.setAwake(false);
		}
		
		super.tick();
	}
	
//	@Override
//	protected ResourceLocation getLootTable() {
//		return this.getType().getLootTable();
//	}
	
	@Override
	protected IParticleData getSquishParticle() {
		return new BlockParticleData(ParticleTypes.BLOCK, AetherBlocks.SENTRY_STONE.getDefaultState());
	}
	
	@Override
	public void applyEntityCollision(Entity entityIn) {
		super.applyEntityCollision(entityIn);
		
		if (!(entityIn instanceof SentryEntity) && entityIn instanceof LivingEntity) {
			this.explodeAt((LivingEntity)entityIn);
		}
	}
	
	protected void explodeAt(LivingEntity entityIn) {
		if (this.isAwake() && this.canEntityBeSeen(entityIn) && entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0f) && this.ticksExisted > 20) {
			entityIn.addVelocity(0.5, 0.5, 0.5);
			
			this.world.createExplosion(this, this.getPosX(), this.getPosY(), this.getPosZ(), 0.1f, Explosion.Mode.DESTROY);
			this.setHealth(0.0f);
			this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 0.2f*(this.rand.nextFloat() - this.rand.nextFloat()) + 1);
			this.applyEnchantments(this, entityIn);
		}
	}
	
	@Override
	protected void jump() {
		if (this.isAwake()) {
			super.jump();
		}
	}
	
	public void setAwake(boolean isAwake) {
		this.dataManager.set(SENTRY_AWAKE, isAwake);
	}
	
	public boolean isAwake() {
		return this.dataManager.get(SENTRY_AWAKE);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public EntityType<? extends SentryEntity> getType() {
		return (EntityType<? extends SentryEntity>) super.getType();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void remove(boolean keepData) {
		this.removed = true;
		super.remove(keepData);
	}
	
	public static class AttackGoal extends SlimeEntity.AttackGoal {
		private final SentryEntity sentry;
		
		public AttackGoal(SentryEntity sentryIn) {
			super(sentryIn);
			this.sentry = sentryIn;
		}
		
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute() {
			return this.sentry.isAwake() && super.shouldExecute();
		}
		
		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			return this.sentry.isAwake() && super.shouldContinueExecuting();
		}
		
	}
	
	public static class FaceRandomGoal extends SlimeEntity.FaceRandomGoal {
		private final SentryEntity sentry;
		
		public FaceRandomGoal(SentryEntity sentryIn) {
			super(sentryIn);
			this.sentry = sentryIn;
		}
		
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute() {
			return this.sentry.isAwake() && super.shouldExecute();
		}
		
		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			return this.sentry.isAwake() && super.shouldContinueExecuting();
		}
		
	}
	
	public static class FloatGoal extends SlimeEntity.FloatGoal {
		private final SentryEntity sentry;
		
		public FloatGoal(SentryEntity sentryIn) {
			super(sentryIn);
			this.sentry = sentryIn;
		}
		
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute() {
			return this.sentry.isAwake() && super.shouldExecute();
		}
		
		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			return this.sentry.isAwake() && super.shouldContinueExecuting();
		}
		
	}
	
	public static class HopGoal extends SlimeEntity.HopGoal {
		private final SentryEntity sentry;
		
		public HopGoal(SentryEntity sentryIn) {
			super(sentryIn);
			this.sentry = sentryIn;
		}
		
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute() {
			return this.sentry.isAwake() && super.shouldExecute();
		}
		
		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			return this.sentry.isAwake() && super.shouldContinueExecuting();
		}
		
	}
	
}