package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.ability.combo.ComboManager;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirBubble;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSuction;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.airbending.FlightAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.airbending.Tornado;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.ChiCombo;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.chiblocking.ChiPassive;
import com.projectkorra.projectkorra.chiblocking.HighJump;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.RapidPunch;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.chiblocking.WarriorStance;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.Catapult;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.earthbending.CompactColumn;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthColumn;
import com.projectkorra.projectkorra.earthbending.EarthGrab;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.earthbending.EarthPassive;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthTunnel;
import com.projectkorra.projectkorra.earthbending.EarthWall;
import com.projectkorra.projectkorra.earthbending.Extraction;
import com.projectkorra.projectkorra.earthbending.LavaFlow;
import com.projectkorra.projectkorra.earthbending.LavaFlow.AbilityType;
import com.projectkorra.projectkorra.earthbending.LavaSurge;
import com.projectkorra.projectkorra.earthbending.LavaWave;
import com.projectkorra.projectkorra.earthbending.MetalClips;
import com.projectkorra.projectkorra.earthbending.SandSpout;
import com.projectkorra.projectkorra.earthbending.Shockwave;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.event.PlayerBendingDeathEvent;
import com.projectkorra.projectkorra.firebending.ArcOfFire;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.Enflamed;
import com.projectkorra.projectkorra.firebending.Extinguish;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBurst;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.FireStream;
import com.projectkorra.projectkorra.firebending.Fireball;
import com.projectkorra.projectkorra.firebending.HeatControl;
import com.projectkorra.projectkorra.firebending.Illumination;
import com.projectkorra.projectkorra.firebending.Lightning;
import com.projectkorra.projectkorra.firebending.RingOfFire;
import com.projectkorra.projectkorra.firebending.WallOfFire;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.HorizontalVelocityChangeEvent;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.Bloodbending;
import com.projectkorra.projectkorra.waterbending.FreezeMelt;
import com.projectkorra.projectkorra.waterbending.IceBlast;
import com.projectkorra.projectkorra.waterbending.IceSpike2;
import com.projectkorra.projectkorra.waterbending.Melt;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.PlantArmor;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterArms;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterMethods;
import com.projectkorra.projectkorra.waterbending.WaterPassive;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterWall;
import com.projectkorra.projectkorra.waterbending.WaterWave;
import com.projectkorra.projectkorra.waterbending.Wave;

public class PKListener implements Listener {

	ProjectKorra plugin;

	public static HashMap<Player, String> bendingDeathPlayer = new HashMap<Player, String>(); // Player
																								// killed
																								// by
																								// Bending

	public PKListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public static void login(BendingPlayer pl) {
		ProjectKorra plugin = ProjectKorra.plugin;
		Player player = Bukkit.getPlayer(pl.getUUID());

		if (player == null) {
			return;
		}

		if (GeneralMethods.toggedOut.contains(player.getUniqueId())) {
			GeneralMethods.getBendingPlayer(player.getName()).toggleBending();
			player.sendMessage(ChatColor.YELLOW
					+ "Reminder, you toggled your bending before signing off. Enable it again with /bending toggle.");
		}

		Preset.loadPresets(player);
		String append = "";
		boolean chatEnabled = ProjectKorra.plugin.getConfig().getBoolean("Properties.Chat.Enable");
		if ((player.hasPermission("bending.avatar")
				|| GeneralMethods.getBendingPlayer(player.getName()).getElements().size() > 1) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Avatar");
		} else if (GeneralMethods.isBender(player.getName(), Element.Air) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Air");
		} else if (GeneralMethods.isBender(player.getName(), Element.Water) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Water");
		} else if (GeneralMethods.isBender(player.getName(), Element.Earth) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Earth");
		} else if (GeneralMethods.isBender(player.getName(), Element.Fire) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Fire");
		} else if (GeneralMethods.isBender(player.getName(), Element.Chi) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Chi");
		}

		if (chatEnabled) {
			player.setDisplayName(append + player.getDisplayName());
		}

		// Handle the AirSpout/WaterSpout login glitches
		if (player.getGameMode() != GameMode.CREATIVE) {
			HashMap<Integer, String> bound = GeneralMethods.getBendingPlayer(player.getName()).getAbilities();
			for (String str : bound.values()) {
				if (str.equalsIgnoreCase("AirSpout") || str.equalsIgnoreCase("WaterSpout")
						|| str.equalsIgnoreCase("SandSpout")) {
					final Player fplayer = player;
					new BukkitRunnable() {
						public void run() {
							fplayer.setFlying(false);
							fplayer.setAllowFlight(false);
						}
					}.runTaskLater(ProjectKorra.plugin, 2);
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (WaterWall.wasBrokenFor(player, block) || OctopusForm.wasBrokenFor(player, block)
				|| Torrent.wasBrokenFor(player, block) || WaterWave.wasBrokenFor(player, block)) {
			event.setCancelled(true);
			return;
		}
		EarthBlast blast = EarthBlast.getBlastFromSource(block);
		if (blast != null) {
			blast.cancel();
		}

		if (FreezeMelt.frozenblocks.containsKey(block)) {
			FreezeMelt.thaw(block);
			event.setCancelled(true);
			// } else if (!WalkOnWater.canThaw(block)) {
			// WalkOnWater.thaw(block);
		} else if (WaterWall.wallblocks.containsKey(block)) {
			WaterWall.thaw(block);
			event.setCancelled(true);
		} else if (Illumination.blocks.containsKey(block)) {
			event.setCancelled(true);
			// } else if (Illumination.blocks.containsKey(block
			// .getRelative(BlockFace.UP))) {
			// event.setCancelled(true);
		} else if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			event.setCancelled(true);
			// event.setCancelled(true);
		} else if (EarthMethods.movedearth.containsKey(block)) {
			// Methods.removeEarthbendedBlockIndex(block);
			EarthMethods.removeRevertIndex(block);
		} else if (TempBlock.isTempBlock(block)) {
			TempBlock.revertBlock(block, Material.AIR);
		} else if (FireMethods.tempFire.keySet().contains(block.getLocation())) {
			FireMethods.revertTempFire(block.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		if (event.isCancelled())
			return;
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (EarthMethods.isLava(fromblock)) {
			event.setCancelled(!EarthPassive.canFlowFromTo(fromblock, toblock));
		}
		if (WaterMethods.isWater(fromblock)) {
			event.setCancelled(!AirBubble.canFlowTo(toblock));
			if (!event.isCancelled()) {
				event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock, toblock));
			}
			if (!event.isCancelled()) {
				if (Illumination.blocks.containsKey(toblock))
					toblock.setType(Material.AIR);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (event.isCancelled())
			return;
		if (TempBlock.isTempBlock(event.getBlock()))
			event.setCancelled(true);
		if (!WaterManipulation.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
		if (!EarthPassive.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled())
			return;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		if (event.isCancelled())
			return;

		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		event.setCancelled(Illumination.blocks.containsKey(block));
		if (!event.isCancelled()) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!EarthPassive.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(FreezeMelt.frozenblocks.containsKey(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Wave.canThaw(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Torrent.canThaw(block));
		}
		if (FireStream.ignitedblocks.containsKey(block)) {
			FireStream.remove(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled())
			return;

		Block block = event.getBlock();
		event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		event.setCancelled(!EarthPassive.canPhysicsChange(block));
		if (!event.isCancelled())
			event.setCancelled(Illumination.blocks.containsKey(block));
		if (!event.isCancelled())
			event.setCancelled(EarthMethods.tempnophysics.contains(block));
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (Paralyze.isParalyzed(player) || ChiCombo.isParalyzed(player) || Bloodbending.isBloodbended(player)
				|| Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
				|| Suffocate.isBreathbent(entity))
			event.setCancelled(true);

		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			if (LavaSurge.falling.contains(entity)) {
				LavaSurge.falling.remove(entity);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.ignitedblocks.containsKey(block) && entity instanceof LivingEntity) {
			new Enflamed(entity, FireStream.ignitedblocks.get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageBlock(EntityDamageByBlockEvent event) {

	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
		if (event.getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
			if (event.getDamager() == null) {
				event.setCancelled(true);
			}
		}

		if (event.getDamager() != null) {
			if (LavaWave.isBlockInWave(event.getDamager())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();

		if (event.getCause() == DamageCause.FIRE
				&& FireStream.ignitedblocks.containsKey(entity.getLocation().getBlock())) {
			new Enflamed(entity, FireStream.ignitedblocks.get(entity.getLocation().getBlock()));
		}

		if (Enflamed.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			Enflamed.dealFlameDamage(entity);
		}

		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (GeneralMethods.getBoundAbility(player) != null
					&& GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HeatControl")) {
				if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
					player.setFireTicks(0);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled())
			return;

		for (Block block : event.blockList()) {
			EarthBlast blast = EarthBlast.getBlastFromSource(block);

			if (blast != null) {
				blast.cancel();
			}
			if (FreezeMelt.frozenblocks.containsKey(block)) {
				FreezeMelt.thaw(block);
			}
			if (WaterWall.wallblocks.containsKey(block)) {
				block.setType(Material.AIR);
			}
			if (!Wave.canThaw(block)) {
				Wave.thaw(block);
			}
			if (EarthMethods.movedearth.containsKey(block)) {
				EarthMethods.removeRevertIndex(block);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity != null)
			if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
					|| Suffocate.isBreathbent(entity))
				event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(EntityInteractEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
				|| Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(ProjectileLaunchEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
				|| Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
				|| Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(SlimeSplitEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
				|| Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntitySuffocatedByTempBlocks(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		if (event.getCause() == DamageCause.SUFFOCATION) {
			if (TempBlock.isTempBlock(event.getEntity().getLocation().add(0, 1, 0).getBlock())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(EntityTeleportEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiCombo.isParalyzed(entity) || Bloodbending.isBloodbended(entity)
				|| Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler
	public void onHorizontalCollision(HorizontalVelocityChangeEvent e) {
		if (!plugin.getConfig().getBoolean("Properties.HorizontalCollisionPhysics.Enabled"))
			return;

		if (e.getEntity() instanceof LivingEntity) {
			if (e.getEntity().getEntityId() != e.getInstigator().getEntityId()) {
				double minimumDistance = plugin.getConfig()
						.getDouble("Properties.HorizontalCollisionPhysics.WallDamageMinimumDistance");
				double damage = ((e.getDistanceTraveled() - minimumDistance) < 0 ? 0
						: e.getDistanceTraveled() - minimumDistance) / (e.getDifference().length());
				if (damage > 0)
					GeneralMethods.damageEntity(e.getInstigator(), e.getEntity(), damage);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled())
			return;

		for (Player p : MetalClips.instances.keySet()) {
			if (MetalClips.instances.get(p).getTarget() != null)
				if (MetalClips.instances.get(p).getTarget().getEntityId() == event.getWhoClicked().getEntityId())
					event.setCancelled(true);
		}

		if (event.getSlotType() == SlotType.ARMOR && !EarthArmor.canRemoveArmor((Player) event.getWhoClicked()))
			event.setCancelled(true);
		if (event.getSlotType() == SlotType.ARMOR && !PlantArmor.canRemoveArmor((Player) event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBendingDeath(PlayerBendingDeathEvent event) {
		if (ConfigManager.deathMsgConfig.get().getBoolean("Properties.Enabled")) {
			bendingDeathPlayer.put(event.getVictim(), event.getAbility());
			final Player player = event.getVictim();

			new BukkitRunnable() {
				@Override
				public void run() {
					bendingDeathPlayer.remove(player);
				}
			}.runTaskLater(ProjectKorra.plugin, 20);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		if (!plugin.getConfig().getBoolean("Properties.Chat.Enable")) {
			return;
		}

		Player player = event.getPlayer();
		ChatColor color = ChatColor.WHITE;

		if (player.hasPermission("bending.avatar")
				|| GeneralMethods.getBendingPlayer(player.getName()).getElements().size() > 1) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Avatar"));
		} else if (GeneralMethods.isBender(player.getName(), Element.Air)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Air"));
		} else if (GeneralMethods.isBender(player.getName(), Element.Water)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Water"));
		} else if (GeneralMethods.isBender(player.getName(), Element.Earth)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Earth"));
		} else if (GeneralMethods.isBender(player.getName(), Element.Fire)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Fire"));
		} else if (GeneralMethods.isBender(player.getName(), Element.Chi)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Chi"));
		}

		String format = plugin.getConfig().getString("Properties.Chat.Format");
		format = format.replace("<message>", "%2$s");
		format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
		event.setFormat(format);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			if (GeneralMethods.isBender(player.getName(), Element.Earth) && event.getCause() == DamageCause.FALL) {
				Shockwave.fallShockwave(player);
			}

			if (GeneralMethods.isBender(player.getName(), Element.Air) && event.getCause() == DamageCause.FALL
					&& GeneralMethods.canBendPassive(player.getName(), Element.Air)) {
				new Flight(player);
				player.setAllowFlight(true);
				AirBurst.fallBurst(player);
				player.setFallDistance(0);
				event.setDamage(0D);
				event.setCancelled(true);
			}

			if (!event.isCancelled() && GeneralMethods.isBender(player.getName(), Element.Water)
					&& event.getCause() == DamageCause.FALL
					&& GeneralMethods.canBendPassive(player.getName(), Element.Water)) {
				if (WaterPassive.applyNoFall(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled() && GeneralMethods.isBender(player.getName(), Element.Earth)
					&& event.getCause() == DamageCause.FALL
					&& GeneralMethods.canBendPassive(player.getName(), Element.Earth)) {
				if (EarthPassive.softenLanding(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled() && GeneralMethods.isBender(player.getName(), Element.Chi)
					&& event.getCause() == DamageCause.FALL
					&& GeneralMethods.canBendPassive(player.getName(), Element.Chi)) {
				if (player.isSprinting()) {
					event.setDamage(0);
					event.setCancelled(true);
				} else {
					double initdamage = event.getDamage();
					double newdamage = event.getDamage() * ChiPassive.FallReductionFactor;
					double finaldamage = initdamage - newdamage;
					event.setDamage(finaldamage);
				}
			}

			if (!event.isCancelled() && event.getCause() == DamageCause.FALL) {
				Player source = Flight.getLaunchedBy(player);
				if (source != null) {
					event.setCancelled(true);
					GeneralMethods.damageEntity(source, player, event.getDamage());
				}
			}

			if (GeneralMethods.canBendPassive(player.getName(), Element.Fire)
					&& GeneralMethods.isBender(player.getName(), Element.Fire)
					&& (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK)) {
				event.setCancelled(!Extinguish.canBurn(player));
			}

			if (GeneralMethods.isBender(player.getName(), Element.Earth) && event.getCause() == DamageCause.SUFFOCATION
					&& TempBlock.isTempBlock(player.getEyeLocation().getBlock())) {
				event.setDamage(0D);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;

		Entity source = e.getDamager();
		Entity entity = e.getEntity();
		Fireball fireball = Fireball.getFireball(source);

		if (fireball != null) {
			e.setCancelled(true);
			fireball.dealDamage(entity);
			return;
		}

		// if (Combustion.fireballs.contains(source.getEntityId())) {
		// e.setCancelled(true);
		// }

		if (Paralyze.isParalyzed(e.getDamager()) || ChiCombo.isParalyzed(e.getDamager())) {
			e.setCancelled(true);
			return;
		}

		if (entity instanceof Player) {
			Suffocate.remove((Player) entity);
		}

		Entity en = e.getEntity();
		if (en instanceof Player) {
			// Player p = (Player) en; // This is the player getting hurt.
			if (e.getDamager() instanceof Player) { // This is the player
													// hitting someone.
				Player sourceplayer = (Player) e.getDamager();
				Player targetplayer = (Player) e.getEntity();
				if (GeneralMethods.canBendPassive(sourceplayer.getName(), Element.Chi)) {
					if (GeneralMethods.isBender(sourceplayer.getName(), Element.Chi)
							&& e.getCause() == DamageCause.ENTITY_ATTACK && e.getDamage() == 1) {
						if (ChiMethods.isChiAbility(GeneralMethods.getBoundAbility(sourceplayer))) {
							if (GeneralMethods.isWeapon(sourceplayer.getItemInHand().getType())
									&& !plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
								return;
							}
							if (ChiPassive.willChiBlock(sourceplayer, targetplayer)) {
								if (GeneralMethods.getBoundAbility(sourceplayer) != null
										&& GeneralMethods.getBoundAbility(sourceplayer).equalsIgnoreCase("Paralyze")) {
									new Paralyze(sourceplayer, targetplayer);
								} else {
									ChiPassive.blockChi(targetplayer);
								}
							}
						}
						// if
						// (sourceplayer.getLocation().distance(targetplayer.getLocation())
						// <=
						// plugin.getConfig().getDouble("Abilities.Chi.RapidPunch.Distance")
						// && Methods.getBoundAbility(sourceplayer) == null) {
						// if
						// (Methods.isWeapon(sourceplayer.getItemInHand().getType())
						// &&
						// !plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons"))
						// {
						// return;
						// } else {
						// if (ChiPassive.willChiBlock(targetplayer)) {
						// ChiPassive.blockChi(targetplayer);
						//
						// }
						// }
						// }
					}
				}
				if (GeneralMethods.canBendPassive(sourceplayer.getName(), Element.Chi)) {
					if (GeneralMethods.isWeapon(sourceplayer.getItemInHand().getType())
							&& !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
						return;
					}
					if (e.getCause() == DamageCause.ENTITY_ATTACK) {
						if (GeneralMethods.getBoundAbility(sourceplayer) != null
								&& GeneralMethods.getBoundAbility(sourceplayer).equalsIgnoreCase("Paralyze")
								&& e.getDamage() == 1) {
							if (ChiPassive.willChiBlock(sourceplayer, targetplayer)) {
								if (CoreAbility.getAbilitiesFromPlayer(sourceplayer).isEmpty()) {
									new Paralyze(sourceplayer, targetplayer);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getKiller() == null) {
			return;
		}
		if (EarthArmor.instances.containsKey(event.getEntity())) {
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			for (int i = 0; i < drops.size(); i++) {
				if (!(drops.get(i).getType() == Material.LEATHER_BOOTS
						|| drops.get(i).getType() == Material.LEATHER_CHESTPLATE
						|| drops.get(i).getType() == Material.LEATHER_HELMET
						|| drops.get(i).getType() == Material.LEATHER_LEGGINGS
						|| drops.get(i).getType() == Material.AIR))
					newdrops.add((drops.get(i)));
			}
			if (EarthArmor.instances.get(event.getEntity()).oldarmor != null) {
				for (ItemStack is : EarthArmor.instances.get(event.getEntity()).oldarmor) {
					if (!(is.getType() == Material.AIR))
						newdrops.add(is);
				}
			}
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			EarthArmor.removeEffect(event.getEntity());
		}
		if (PlantArmor.instances.containsKey(event.getEntity())) {
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			for (int i = 0; i < drops.size(); i++) {
				if (!(drops.get(i).getType() == Material.LEATHER_BOOTS
						|| drops.get(i).getType() == Material.LEATHER_CHESTPLATE
						|| drops.get(i).getType() == Material.LEAVES || drops.get(i).getType() == Material.LEAVES_2
						|| drops.get(i).getType() == Material.LEATHER_LEGGINGS
						|| drops.get(i).getType() == Material.AIR))
					newdrops.add((drops.get(i)));
			}
			if (PlantArmor.instances.get(event.getEntity()).oldarmor != null) {
				for (ItemStack is : PlantArmor.instances.get(event.getEntity()).oldarmor) {
					if (!(is.getType() == Material.AIR))
						newdrops.add(is);
				}
			}
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			PlantArmor.removeEffect(event.getEntity());
		}
		if (MetalClips.instances.containsKey(event.getEntity())) {
			MetalClips.instances.get(event.getEntity()).remove();
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			for (int i = 0; i < drops.size(); i++) {
				if (!(drops.get(i).getType() == Material.IRON_HELMET
						|| drops.get(i).getType() == Material.IRON_CHESTPLATE
						|| drops.get(i).getType() == Material.IRON_LEGGINGS
						|| drops.get(i).getType() == Material.IRON_BOOTS || drops.get(i).getType() == Material.AIR))
					newdrops.add((drops.get(i)));
			}
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);

		}
		if (bendingDeathPlayer.containsKey(event.getEntity())) {
			String message = ConfigManager.deathMsgConfig.get().getString("Properties.Default");
			String ability = bendingDeathPlayer.get(event.getEntity());
			String element = null;
			Player killer = event.getEntity().getKiller();
			String killerAbility = GeneralMethods.getLastUsedAbility(killer, false);
			if (ability == null) {
				ability = GeneralMethods.getLastUsedAbility(killer, false);
				if (ComboManager.checkForValidCombo(killer) != null) {
					String combo = ComboManager.checkForValidCombo(killer).getName();
					if (combo != null && !combo.isEmpty()) {
						element = GeneralMethods.getAbilityElement(killerAbility).name();
						ability = element + "Combo";
					}
				} else if (ability != null && GeneralMethods.abilityExists(ability)) {
					element = GeneralMethods.getAbilityElement(ability).name();
				} else {
					bendingDeathPlayer.remove(event.getEntity());
					return;
				}
			}
			if (ConfigManager.deathMsgConfig.get().contains(element + "." + ability)) {
				message = ConfigManager.deathMsgConfig.get().getString(element + "." + ability);
			}
			message = message.replace("{victim}", event.getEntity().getName())
					.replace("{attacker}", event.getEntity().getKiller().getName())
					.replace("{ability}", GeneralMethods.getAbilityColor(killerAbility) + ability);
			event.setDeathMessage(message);
			bendingDeathPlayer.remove(event.getEntity());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteraction(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			String ability = GeneralMethods.getBoundAbility(player);
			ComboManager.addComboAbility(player, ClickType.RIGHT_CLICK);
			if (ability != null && ability.equalsIgnoreCase("EarthSmash"))
				new EarthSmash(player, ClickType.RIGHT_CLICK);
		}
		if (Paralyze.isParalyzed(player) || ChiCombo.isParalyzed(player) || Bloodbending.isBloodbended(player)
				|| Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		GeneralMethods.createBendingPlayer(e.getPlayer().getUniqueId(), player.getName());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled())
			return;
		FlightAbility.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}

		if (ChiCombo.isParalyzed(player)) {
			event.setTo(event.getFrom());
			return;
		}

		if (WaterSpout.instances.containsKey(event.getPlayer()) || AirSpout.getPlayers().contains(event.getPlayer())
				|| SandSpout.getPlayers().contains(event.getPlayer())) {
			Vector vel = new Vector();
			vel.setX(event.getTo().getX() - event.getFrom().getX());
			vel.setY(event.getTo().getY() - event.getFrom().getY());
			vel.setZ(event.getTo().getZ() - event.getFrom().getZ());
			// You now know the old velocity. Set to match recommended velocity
			double currspeed = vel.length();
			double maxspeed = .15;
			if (currspeed > maxspeed) {
				// only if moving set a factor
				// double recspeed = 0.6;
				// vel = vel.ultiply(recspeed * currspeed);
				vel = vel.normalize().multiply(maxspeed);
				// apply the new velocity (MAY REQUIRE A SCHEDULED TASK
				// INSTEAD!)
				event.getPlayer().setVelocity(vel);
			}
		}

		if (Bloodbending.isBloodbended(player)) {
			double distance1, distance2;
			Location loc = Bloodbending.getBloodbendingLocation(player);
			distance1 = event.getFrom().distance(loc);
			distance2 = event.getTo().distance(loc);
			if (distance2 > distance1) {
				player.setVelocity(new Vector(0, 0, 0));
			}
		}

		if (FlightAbility.contains(event.getPlayer())) {
			if (FlightAbility.isHovering(event.getPlayer())) {
				Location loc = event.getFrom();
				Location toLoc = player.getLocation();

				if (loc.getX() != toLoc.getX() || loc.getY() != toLoc.getY() || loc.getZ() != toLoc.getZ()) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer != null) {
			if (GeneralMethods.toggedOut.contains(player.getUniqueId()) && bPlayer.isToggled())
				GeneralMethods.toggedOut.remove(player.getUniqueId());
			if (!bPlayer.isToggled())
				GeneralMethods.toggedOut.add(player.getUniqueId());
		}

		if (Commands.invincible.contains(event.getPlayer().getName())) {
			Commands.invincible.remove(event.getPlayer().getName());
		}
		Preset.unloadPreset(player);
		BendingPlayer.getPlayers().remove(event.getPlayer().getUniqueId());
		if (EarthArmor.instances.containsKey(event.getPlayer())) {
			EarthArmor.removeEffect(event.getPlayer());
			event.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		}
		if (PlantArmor.instances.containsKey(event.getPlayer())) {
			PlantArmor.removeEffect(event.getPlayer());
		}

		for (Player p : MetalClips.instances.keySet()) {
			if (MetalClips.instances.get(p).getTarget() != null
					&& MetalClips.instances.get(p).getTarget().getEntityId() == event.getPlayer().getEntityId()) {
				MetalClips.instances.get(p).remove();
			}
		}

		MultiAbilityManager.remove(player);
		FlightAbility.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();

		if (event.isCancelled())
			return;

		if (player.isSneaking())
			ComboManager.addComboAbility(player, ClickType.SHIFT_UP);
		else
			ComboManager.addComboAbility(player, ClickType.SHIFT_DOWN);

		if (Suffocate.isBreathbent(player)) {
			if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirSwipe")
					|| !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("FireBlast")
					|| !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("EarthBlast")
					|| !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterManipulation")) {
				event.setCancelled(true);
			}
		}

		if (Paralyze.isParalyzed(player) || ChiCombo.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking()) {
			BlockSource.update(player, ClickType.SHIFT_DOWN);
		}

		if (!player.isSneaking() && WaterArms.hasPlayer(player)) {
			WaterArms.displayBoundMsg(player);
			return;
		}

		AirScooter.check(player);

		String abil = GeneralMethods.getBoundAbility(player);
		if (abil == null) {
			return;
		}

		if (ChiMethods.isChiBlocked(player.getName())) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking() && GeneralMethods.canBend(player.getName(), abil)) {
			if (GeneralMethods.isDisabledStockAbility(abil))
				return;
			if (AirMethods.isAirAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Tornado")) {
					new Tornado(player);
				}
				if (abil.equalsIgnoreCase("AirBlast")) {
					AirBlast.setOrigin(player);
				}
				if (abil.equalsIgnoreCase("AirBurst")) {
					new AirBurst(player);
				}
				if (abil.equalsIgnoreCase("AirSuction")) {
					AirSuction.setOrigin(player);
				}
				if (abil.equalsIgnoreCase("AirSwipe")) {
					AirSwipe.charge(player);
				}
				if (abil.equalsIgnoreCase("AirShield")) {
					new AirShield(player);
				}
				if (abil.equalsIgnoreCase("Suffocate")) {
					new Suffocate(player);
				}
				if (abil.equalsIgnoreCase("Flight")) {
					if (player.isSneaking() || !AirMethods.canAirFlight(player))
						return;
					new FlightAbility(player);
				}
			}

			if (WaterMethods.isWaterAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Bloodbending")) {
					new Bloodbending(player);
				}
				if (abil.equalsIgnoreCase("IceBlast")) {
					new IceBlast(player);
				}
				if (abil.equalsIgnoreCase("IceSpike")) {
					new IceSpike2(player);
				}
				if (abil.equalsIgnoreCase("OctopusForm")) {
					OctopusForm.form(player);
				}
				if (abil.equalsIgnoreCase("PhaseChange")) {
					new Melt(player);
				}
				if (abil.equalsIgnoreCase("WaterManipulation")) {
					new WaterManipulation(player);
				}
				if (abil.equalsIgnoreCase("Surge")) {
					WaterWall.form(player);
				}
				if (abil.equalsIgnoreCase("Torrent")) {
					Torrent.create(player);
				}
				if (abil.equalsIgnoreCase("WaterArms")) {
					new WaterArms(player);
				}
			}

			if (EarthMethods.isEarthAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("EarthBlast")) {
					new EarthBlast(player);
				}
				if (abil.equalsIgnoreCase("RaiseEarth")) {
					new EarthWall(player);
				}
				if (abil.equalsIgnoreCase("Collapse")) {
					new Collapse(player);
				}
				if (abil.equalsIgnoreCase("Shockwave")) {
					new Shockwave(player);
				}
				if (abil.equalsIgnoreCase("EarthGrab")) {
					EarthGrab.EarthGrabSelf(player);
				}
				if (abil.equalsIgnoreCase("EarthTunnel")) {
					new EarthTunnel(player);
				}
				if (abil.equalsIgnoreCase("Tremorsense")) {
					GeneralMethods.getBendingPlayer(player.getName()).toggleTremorSense();
				}
				if (abil.equalsIgnoreCase("Extraction")) {
					new Extraction(player);
				}
				if (abil.equalsIgnoreCase("MetalClips")) {
					if (MetalClips.instances.containsKey(player)) {
						if (MetalClips.instances.get(player).getTarget() == null)
							MetalClips.instances.get(player).magnet();
						else
							MetalClips.instances.get(player).control();
					} else
						new MetalClips(player, 1);
				}
				// if (abil.equalsIgnoreCase("LavaSurge")) {
				// new LavaSurge(player);
				// }
				if (abil.equalsIgnoreCase("LavaFlow")) {
					new LavaFlow(player, LavaFlow.AbilityType.SHIFT);
				}
				if (abil.equalsIgnoreCase("EarthSmash")) {
					new EarthSmash(player, ClickType.SHIFT_DOWN);
				}
			}

			if (FireMethods.isFireAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Blaze")) {
					new RingOfFire(player);
				}
				if (abil.equalsIgnoreCase("FireBlast")) {
					new Fireball(player);
				}
				if (abil.equalsIgnoreCase("HeatControl")) {
					new HeatControl(player);
				}
				if (abil.equalsIgnoreCase("FireBurst")) {
					new FireBurst(player);
				}
				if (abil.equalsIgnoreCase("FireShield")) {
					FireShield.shield(player);
				}
				if (abil.equalsIgnoreCase("Lightning")) {
					new Lightning(player);
				}
				if (abil.equalsIgnoreCase("Combustion")) {
					new Combustion(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwing(PlayerAnimationEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		ComboManager.addComboAbility(player, ClickType.LEFT_CLICK);

		if (Suffocate.isBreathbent(player)) {
			if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirSwipe")
					|| !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("FireBlast")
					|| !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("EarthBlast")
					|| !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterManipulation")) {
				event.setCancelled(true);
			}
		}

		if (Bloodbending.isBloodbended(player) || Paralyze.isParalyzed(player) || ChiCombo.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}

		if (ChiMethods.isChiBlocked(player.getName())) {
			event.setCancelled(true);
			return;
		}

		BlockSource.update(player, ClickType.LEFT_CLICK);

		AirScooter.check(player);

		String abil = GeneralMethods.getBoundAbility(player);
		if (abil == null && !MultiAbilityManager.hasMultiAbilityBound(player))
			return;
		if (GeneralMethods.canBend(player.getName(), abil)) {
			if (GeneralMethods.isDisabledStockAbility(abil))
				return;

			if (AirMethods.isAirAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("AirBlast")) {
					new AirBlast(player);
				}
				if (abil.equalsIgnoreCase("AirSuction")) {
					new AirSuction(player);
				}
				if (abil.equalsIgnoreCase("AirBurst")) {
					AirBurst.coneBurst(player);
				}
				if (abil.equalsIgnoreCase("AirScooter")) {
					new AirScooter(player);
				}
				if (abil.equalsIgnoreCase("AirSpout")) {
					new AirSpout(player);
				}
				if (abil.equalsIgnoreCase("AirSwipe")) {
					new AirSwipe(player);
				}
				if (abil.equalsIgnoreCase("Flight")) {
					if (!ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.Flight.HoverEnabled")
							|| !AirMethods.canAirFlight(player))
						return;

					if (FlightAbility.contains(event.getPlayer())) {
						if (FlightAbility.isHovering(event.getPlayer())) {
							FlightAbility.setHovering(event.getPlayer(), false);
						} else {
							FlightAbility.setHovering(event.getPlayer(), true);
						}
					}
				}
			}

			if (WaterMethods.isWaterAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Bloodbending")) {
					Bloodbending.launch(player);
				}
				if (abil.equalsIgnoreCase("IceBlast")) {
					IceBlast.activate(player);
				}
				if (abil.equalsIgnoreCase("IceSpike")) {
					IceSpike2.activate(player);
				}
				if (abil.equalsIgnoreCase("OctopusForm")) {
					new OctopusForm(player);
				}
				if (abil.equalsIgnoreCase("PhaseChange")) {
					new FreezeMelt(player);
				}
				if (abil.equalsIgnoreCase("PlantArmor")) {
					new PlantArmor(player);
				}
				if (abil.equalsIgnoreCase("WaterSpout")) {
					new WaterSpout(player);
				}
				if (abil.equalsIgnoreCase("WaterManipulation")) {
					WaterManipulation.moveWater(player);
				}
				if (abil.equalsIgnoreCase("Surge")) {
					new WaterWall(player);
				}
				if (abil.equalsIgnoreCase("Torrent")) {
					new Torrent(player);
				}
			}

			if (EarthMethods.isEarthAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Catapult")) {
					new Catapult(player);
				}
				if (abil.equalsIgnoreCase("EarthBlast")) {
					EarthBlast.throwEarth(player);
				}
				if (abil.equalsIgnoreCase("RaiseEarth")) {
					new EarthColumn(player);
				}
				if (abil.equalsIgnoreCase("Collapse")) {
					new CompactColumn(player);
				}
				if (abil.equalsIgnoreCase("Shockwave")) {
					Shockwave.coneShockwave(player);
				}
				if (abil.equalsIgnoreCase("EarthArmor")) {
					new EarthArmor(player);
				}
				if (abil.equalsIgnoreCase("EarthGrab")) {
					new EarthGrab(player);
				}
				if (abil.equalsIgnoreCase("Tremorsense")) {
					new Tremorsense(player);
				}
				if (abil.equalsIgnoreCase("MetalClips")) {
					if (!MetalClips.instances.containsKey(player)) {
						new MetalClips(player, 0);
					} else if (MetalClips.instances.containsKey(player)) {
						if (MetalClips.instances
								.get(player).metalclips < (player.hasPermission("bending.ability.MetalClips.4clips") ? 4
										: 3))
							MetalClips.instances.get(player).shootMetal();
						else
							MetalClips.instances.get(player).launch();
					}
				}
				if (abil.equalsIgnoreCase("LavaSurge")) {
					if (LavaSurge.instances.containsKey(player))
						LavaSurge.instances.get(player).launch();
				}
				if (abil.equalsIgnoreCase("LavaFlow")) {
					new LavaFlow(player, AbilityType.CLICK);
				}
				if (abil.equalsIgnoreCase("EarthSmash")) {
					new EarthSmash(player, ClickType.LEFT_CLICK);
				}
				if (abil.equalsIgnoreCase("SandSpout")) {
					new SandSpout(player);
				}
			}

			if (FireMethods.isFireAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Blaze")) {
					new ArcOfFire(player);
				}
				if (abil.equalsIgnoreCase("FireBlast")) {
					new FireBlast(player);
				}
				if (abil.equalsIgnoreCase("FireJet")) {
					new FireJet(player);
				}
				if (abil.equalsIgnoreCase("HeatControl")) {
					new Extinguish(player);
				}
				if (abil.equalsIgnoreCase("Illumination")) {
					new Illumination(player);
				}
				if (abil.equalsIgnoreCase("FireBurst")) {
					FireBurst.coneBurst(player);
				}
				if (abil.equalsIgnoreCase("FireShield")) {
					new FireShield(player);
				}
				if (abil.equalsIgnoreCase("WallOfFire")) {
					new WallOfFire(player);
				}
				if (abil.equalsIgnoreCase("Combustion")) {
					Combustion.explode(player);
				}
			}

			if (ChiMethods.isChiAbility(abil)) {
				if (GeneralMethods.isWeapon(player.getItemInHand().getType())
						&& !plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("HighJump")) {
					new HighJump(player);
				}
				if (abil.equalsIgnoreCase("RapidPunch")) {
					new RapidPunch(player);
				}
				if (abil.equalsIgnoreCase("Paralyze")) {
					//
				}
				if (abil.equalsIgnoreCase("Smokescreen")) {
					new Smokescreen(player);
				}
				if (abil.equalsIgnoreCase("WarriorStance")) {
					new WarriorStance(player);
				}
				if (abil.equalsIgnoreCase("AcrobatStance")) {
					new AcrobatStance(player);
				}
				if (abil.equalsIgnoreCase("QuickStrike")) {
					new QuickStrike(player);
				}
				if (abil.equalsIgnoreCase("SwiftKick")) {
					new SwiftKick(player);
				}
			}

			if (abil.equalsIgnoreCase("AvatarState")) {
				new AvatarState(player);
			}
		}

		if (MultiAbilityManager.hasMultiAbilityBound(player)) {
			abil = MultiAbilityManager.getBoundMultiAbility(player);
			if (abil.equalsIgnoreCase("WaterArms")) {
				new WaterArms(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		if (event.isCancelled())
			return;

		Player p = event.getPlayer();
		if (Tornado.getPlayers().contains(p) || Bloodbending.isBloodbended(p) || Suffocate.isBreathbent(p)
				|| FireJet.getPlayers().contains(p) || AvatarState.getPlayers().contains(p)) {
			event.setCancelled(p.getGameMode() != GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Integer id = event.getEntity().getEntityId();
		if (Smokescreen.snowballs.contains(id)) {
			Location loc = event.getEntity().getLocation();
			Smokescreen.playEffect(loc);
			for (Entity en : GeneralMethods.getEntitiesAroundPoint(loc, Smokescreen.radius)) {
				Smokescreen.applyBlindness(en);
			}
			Smokescreen.snowballs.remove(id);
		}
		// if (Combustion.fireballs.contains(id)) {
		// Location loc = event.getEntity().getLocation();
		//// for (Entity en: Methods.getEntitiesAroundPoint(loc, 4)) {
		//// if (en instanceof LivingEntity) {
		//// LivingEntity le = (LivingEntity) en;
		//// le.damage(ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Combustion.Damage"));
		//// }
		//// }
		// }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFall(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (event.getCause() == DamageCause.FALL) {
				if (CoreAbility.getInstances(FireJet.class).containsKey(player.getEntityId())) {
					event.setDamage(0);
				}
				else if (CoreAbility.getInstances(Catapult.class).containsKey(player.getEntityId())) {
					event.setDamage(0);
				}
			}
		}
	}

}
