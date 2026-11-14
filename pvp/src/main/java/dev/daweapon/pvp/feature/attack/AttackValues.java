package dev.daweapon.pvp.feature.attack;

// Holder for the intermediate attack-damage records passed through the critical/sweeping/sound attack pipeline.
public class AttackValues {
	public record PreCritical(float damage, float magicalDamage, double cooldownProgress,
	                          boolean strong, boolean sprint, int knockback, int fireAspect) {
		public PreSweeping withCritical(boolean critical) {
			return new PreSweeping(
					damage, magicalDamage, cooldownProgress,
					strong, sprint, critical, knockback, fireAspect
			);
		}
	}
	
	public record PreSweeping(float damage, float magicalDamage, double cooldownProgress,
	                          boolean strong, boolean sprint, boolean critical,
	                          int knockback, int fireAspect) {
		public PreSounds withSweeping(boolean sweeping) {
			return new PreSounds(
					damage, magicalDamage, cooldownProgress,
					strong, sprint, critical, sweeping,
					knockback, fireAspect
			);
		}
	}
	
	public record PreSounds(float damage, float magicalDamage, double cooldownProgress,
	                  boolean strong, boolean sprint, boolean critical, boolean sweeping,
	                  int knockback, int fireAspect) {}
	
	public record Final(float damage, boolean strong, boolean sprint,
	                    int knockback, boolean critical, boolean magical,
	                    int fireAspect, boolean sweeping, boolean sounds,
	                    boolean playSoundsOnFail) {}
}
