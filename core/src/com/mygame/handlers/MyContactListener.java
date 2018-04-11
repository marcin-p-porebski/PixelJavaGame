package com.mygame.handlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygame.entities.Arrow;
import com.mygame.entities.Loot;
import com.mygame.entities.Player;
import com.mygame.entities.Sprite;
import com.mygame.game.MyGame;
import com.mygame.interfaces.Attackable;

public class MyContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();
        WorldManifold wm = contact.getWorldManifold();

        if (fa.getUserData() == null || fb.getUserData() == null) return;

        playerEnemyCollision(fa, fb, wm);

        swordEnemyCollision(fa, fb);

        playerLootCollision(fa, fb);

        arrowEnemyCollision(fa, fb);

        playerArrowCollision(fa, fb);
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    private void playerEnemyCollision(Fixture fa, Fixture fb, WorldManifold wm) {
        if ((fa.getFilterData().categoryBits == Constants.BIT_PLAYER && fb.getFilterData().categoryBits == Constants.BIT_ENEMY) ||
                (fb.getFilterData().categoryBits == Constants.BIT_PLAYER && fa.getFilterData().categoryBits == Constants.BIT_ENEMY)) {
            Sprite player = (Sprite) fa.getUserData();
            Sprite enemy = (Sprite) fb.getUserData();

            if (!player.toString().equals("player")) {
                player = (Sprite) fb.getUserData();
                enemy = (Sprite) fa.getUserData();
            }

            if (((Attackable) player).getAttackableState() == Attackable.AttackableState.ALIVE &&
                    ((Attackable) enemy).getAttackableState() == Attackable.AttackableState.ALIVE) {
                float impulsePower = 500.f;
                Vector2 n = wm.getNormal();
                player.getBody().applyLinearImpulse(new Vector2(n.x * impulsePower,
                        n.y * impulsePower), player.getBody().getPosition(), true);
                enemy.getBody().applyLinearImpulse(new Vector2(-n.x * impulsePower,
                        -n.y * impulsePower), enemy.getBody().getPosition(), true);

                ((Attackable) player).hit(10);
                MyGame.assets.getSound("hurt01").stop();
                MyGame.assets.getSound("hurt01").play(0.2f);
                ((Attackable) enemy).hit(20);
            }
        }
    }

    private void swordEnemyCollision(Fixture fa, Fixture fb) {
        if ((fa.getFilterData().categoryBits == Constants.BIT_WEAPON && fb.getFilterData().categoryBits == Constants.BIT_ENEMY) ||
                (fb.getFilterData().categoryBits == Constants.BIT_WEAPON && fa.getFilterData().categoryBits == Constants.BIT_ENEMY)) {
            Sprite player = (Sprite) fa.getUserData();
            Sprite enemy = (Sprite) fb.getUserData();

            if (!fa.getUserData().toString().equals("player")) {
                player = (Sprite) fb.getUserData();
                enemy = (Sprite) fa.getUserData();
            }

            if (((Attackable) enemy).getAttackableState() == Attackable.AttackableState.ALIVE &&
                    ((Attackable) player).getAttackableState() == Attackable.AttackableState.ALIVE) {
                float impulsePower = 800.f;

                Vector2 n = new Vector2(player.getPosition().x - enemy.getPosition().x, player.getPosition().y - enemy.getPosition().y);
                enemy.getBody().applyLinearImpulse(new Vector2(-n.x * impulsePower,
                        -n.y * impulsePower), enemy.getBody().getPosition(), true);

                ((Attackable) enemy).hit(30);
                MyGame.assets.getSound("sword01").play();
            }
        }
    }

    private void playerLootCollision(Fixture fa, Fixture fb) {
        if ((fa.getFilterData().categoryBits == Constants.BIT_PLAYER && fb.getFilterData().categoryBits == Constants.BIT_LOOT) ||
                (fb.getFilterData().categoryBits == Constants.BIT_PLAYER && fa.getFilterData().categoryBits == Constants.BIT_LOOT)) {
            Player player;
            Loot loot;

            if (fa.getUserData().toString().equals("player")) {
                player = (Player) fa.getUserData();
                loot = (Loot) fb.getUserData();
            } else {
                player = (Player) fb.getUserData();
                loot = (Loot) fa.getUserData();
            }

            player.lootGold(loot.getGold());
            MyGame.assets.getSound("gold").play();
        }
    }

    private void arrowEnemyCollision(Fixture fa, Fixture fb) {
        if ((fa.getFilterData().categoryBits == Constants.BIT_ARROW && fb.getFilterData().categoryBits == Constants.BIT_ENEMY) ||
                (fb.getFilterData().categoryBits == Constants.BIT_ARROW && fa.getFilterData().categoryBits == Constants.BIT_ENEMY)) {
            Arrow arrow;
            Sprite enemy;

            if (fa.getUserData().toString().equals("arrow")) {
                arrow = (Arrow) fa.getUserData();
                enemy = (Sprite) fb.getUserData();
            } else {
                arrow = (Arrow) fb.getUserData();
                enemy = (Sprite) fa.getUserData();
            }

            if (((Attackable) enemy).getAttackableState() == Attackable.AttackableState.ALIVE &&
                    arrow.isActive()) {
                float impulsePower = 8000.f;

                Vector2 n = new Vector2(arrow.getPosition().x - enemy.getPosition().x, arrow.getPosition().y - enemy.getPosition().y);
                enemy.getBody().applyLinearImpulse(new Vector2(-n.x * impulsePower,
                        -n.y * impulsePower), enemy.getBody().getPosition(), true);

                ((Attackable) enemy).hit(20);
                arrow.getBody().setLinearVelocity(0, 0);
                arrow.getBody().getFixtureList().peek().setSensor(true);
                MyGame.assets.getSound("arrowImpact01").play();
            }
        }
    }

    private void playerArrowCollision(Fixture fa, Fixture fb) {
        if ((fa.getFilterData().categoryBits == Constants.BIT_PLAYER && fb.getFilterData().categoryBits == Constants.BIT_ARROW) ||
                (fb.getFilterData().categoryBits == Constants.BIT_PLAYER && fa.getFilterData().categoryBits == Constants.BIT_ARROW)) {
            Player player;
            Arrow arrow;

            if (fa.getUserData().toString().equals("player")) {
                player = (Player) fa.getUserData();
                arrow = (Arrow) fb.getUserData();
            } else {
                player = (Player) fb.getUserData();
                arrow = (Arrow) fa.getUserData();
            }

            if(!arrow.isActive()) {
                player.lootArrow();
                arrow.setLooted(true);
                MyGame.assets.getSound("arrowPickup").play(0.5f);
            }
        }
    }
}
