package com.android.fishgame;

import java.util.ArrayList;
import java.util.Random;

import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;

import android.view.MotionEvent;


public class FishGameLayer extends CCColorLayer {
	
	//float screenWidth;
	//float screenHeight;//TODO can we use these everywhere??
	
	public static String enemyImage = "Target.png";
	public static String friendlyImage = "Player.png";
	public static String projectileImage = "Projectile.png";
	public static String shrimpImage = "Shrimp.png";

	
	//Sprites
	protected ArrayList<CCSprite> _targets;
	protected ArrayList<CCSprite> _projectiles;
	protected ArrayList<CCSprite> _friendlysprites;
	
	//Objects
	protected ArrayList<Enemy> _enemies;
	protected ArrayList<Friendly> _friendlies;
	
	Lane [] lanes;
	
	protected static int numLanes = 5;
	protected static int numColumns = 9; 
	
	protected int shrimpMeter = 0; 
	
	class Lane {
		int enemyCount;
	}
	
	abstract class Actor {
		Integer health;
		CCSprite sprite;
	}
	
	class Enemy extends Actor {
		Lane lane;
		
		public Enemy() {
			health = 100;
			sprite = CCSprite.sprite(enemyImage);
		}
	}
	
	class Shrimp {
		CCSprite sprite;
		
		public Shrimp(){
			sprite = CCSprite.sprite(shrimpImage);
		}
		
	}
	
	class Friendly extends Actor {
		Lane lane;
		
		public Friendly() {
			health = 100;
			sprite = CCSprite.sprite(friendlyImage);
		}
	}
	
	
	public static CCScene scene()
	{
	    CCScene scene = CCScene.node();
	    CCColorLayer layer = new FishGameLayer(ccColor4B.ccc4(255, 255, 255, 255));	 
	    scene.addChild(layer);
	 
	    return scene;
	}
	
	protected FishGameLayer(ccColor4B color)
	{
		super(color);
		_targets = new ArrayList<CCSprite>();
		_projectiles = new ArrayList<CCSprite>();
		_friendlysprites = new ArrayList<CCSprite>();
		
		_enemies = new ArrayList<Enemy>();
		_friendlies = new ArrayList<Friendly>();
		
		lanes = new Lane[numLanes];
		
		//hasEnemy = new boolean[numLanes];
		for (int i = 0; i < numLanes; i++) {
			lanes[i] = new Lane();
			lanes[i].enemyCount = 0;
			
		}
		
		//CGSize winSize = CCDirector.sharedDirector().displaySize();
	    //screenWidth = winSize.width;
	    //screenHeight = winSize.height;
	    //System.out.println("Height: " + screenHeight + " Width: " + screenWidth);
	  
		this.setIsTouchEnabled(true);
		this.schedule("gameLogic", 5.0f); //TODO was 1.0f
		this.schedule("updateBehavior", 1.0f);
		this.schedule("update", 0.1f);
	}
	
	public void gameLogic(float dt) {
		addTarget();
		addShrimp();
	}
	
	protected void addShrimp(){
		Random rand = new Random();
		//boolean areWeAddingAShrimp = (rand.nextInt(3) == 0) ? true : false ;
		boolean areWeAddingAShrimp = true;
		CGSize winSize = CCDirector.sharedDirector().displaySize();

		
		if (areWeAddingAShrimp){
			int col = rand.nextInt(numColumns);
			int row = rand.nextInt(numLanes);
			float x = (1.0f/col)*winSize.width;
			float y = winSize.height* (row * .18f + .09f);
			
			Shrimp shrimp = new Shrimp();
			shrimp.sprite.setPosition(CGPoint.make(x, y));
			addChild(shrimp.sprite);
		}
	}
	
	//Wrapper version
	protected void addTarget() {
		Random rand = new Random();
		Enemy enemy = new Enemy();
		//CCSprite target = CCSprite.sprite("Target.png");
		
		CGSize winSize = CCDirector.sharedDirector().displaySize();
		int rangeY = 5;
		int row = rand.nextInt(rangeY);
		enemy.lane = lanes[row];
		
		float actualY = winSize.height* (row * .18f + .09f);
		
		enemy.sprite.setPosition(CGPoint.make(winSize.width + (enemy.sprite.getContentSize().width/2.0f), actualY));
		addChild(enemy.sprite);
		enemy.sprite.setTag(1);
		_targets.add(enemy.sprite);
		_enemies.add(enemy);
		
		lanes[row].enemyCount++;
		
		int minDuration = 7;
		int maxDuration = 10;
		int rangeDuration = maxDuration - minDuration;
		int actualDuration = rand.nextInt(rangeDuration) + minDuration;
		
		CCMoveTo actionMove = CCMoveTo.action(actualDuration, CGPoint.ccp(- enemy.sprite.getContentSize().width/2.0f, actualY));
		CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "spriteMoveFinished");
		CCSequence actions = CCSequence.actions(actionMove, actionMoveDone);
		
		enemy.sprite.runAction(actions);
		
	}
	
	/* Sprite Version
	protected void addTarget() {
		Random rand = new Random();
		
		CCSprite target = CCSprite.sprite("Target.png");
		
		CGSize winSize = CCDirector.sharedDirector().displaySize();
		int rangeY = 5;
		float actualY = winSize.height* (rand.nextInt(rangeY) * .18f + .09f);
		
		target.setPosition(winSize.width + (target.getContentSize().width/2.0f), actualY);
		addChild(target);
		target.setTag(1);
		_targets.add(target);
		
		
		int minDuration = 7;
		int maxDuration = 10;
		int rangeDuration = maxDuration - minDuration;
		int actualDuration = rand.nextInt(rangeDuration) + minDuration;
		
		CCMoveTo actionMove = CCMoveTo.action(actualDuration, CGPoint.ccp(-target.getContentSize().width/2.0f, actualY));
		CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "spriteMoveFinished");
		CCSequence actions = CCSequence.actions(actionMove, actionMoveDone);
		
		target.runAction(actions);
		
	}*/

	
	@Override
	public boolean ccTouchesEnded(MotionEvent event)
	{
	    // Choose one of the touches to work with
	    CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
	    
	    
	    // Set up initial location of projectile
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    
	    
	    //ignore left 10%
	    if (location.x < winSize.width/10.0f)
	    	return true;
	    
	    if (location.y > winSize.height * (9.0f/10.0f))
	    	return true;
	    
	    //CCSprite friendly = CCSprite.sprite(friendlyImage);
	 	  Friendly friendly = new Friendly();
	 	  
	    float realX = location.x;
	    float realY = location.y;
	    
	    if (location.x < winSize.width * 0.20f){
	    	realX = winSize.width * .15f;
	    } else if (location.x < winSize.width * 0.30f){
	    	realX = winSize.width * .25f;
	    } else if (location.x < winSize.width * 0.40f){
	    	realX = winSize.width * .35f;
	    } else if (location.x < winSize.width * 0.50f){
	    	realX = winSize.width * .45f;
	    } else if (location.x < winSize.width * 0.60f){
	    	realX = winSize.width * .55f;
	    } else if (location.x < winSize.width * 0.70f){
	    	realX = winSize.width * .65f;
	    } else if (location.x < winSize.width * 0.80f){
	    	realX = winSize.width * .75f;
	    } else if (location.x < winSize.width * 0.90f){
	    	realX = winSize.width * .85f;
	    } else {
	    	realX = winSize.width * .95f;
	    }
	    
	    if (location.y < winSize.height * .18) {
	    	realY = winSize.height *.09f;
	    	friendly.lane = lanes[0];
	    } else if (location.y < winSize.height * .36) {
	    	realY = winSize.height *.27f;
	    	friendly.lane = lanes[1];
	    } else if (location.y < winSize.height * .54) {
	    	realY = winSize.height *.45f;
	    	friendly.lane = lanes[2];
	    } else if (location.y < winSize.height * .72) {
	    	realY = winSize.height *.63f;
	    	friendly.lane = lanes[3];
	    } else {
	    	realY = winSize.height *.81f;
	    	friendly.lane = lanes[4];
	    } 
	    	
	    friendly.sprite.setPosition(CGPoint.make(realX, realY));
	 
	    // Determine offset of location to projectile
	    //int offX = (int)(location.x - projectile.getPosition().x);
	    //int offY = (int)(location.y - projectile.getPosition().y);
	 
	    // Bail out if we are shooting down or backwards
	    //if (offX <= 0)
	      //  return true;
	 
	    // Ok to add now - we've double checked position
	    addChild(friendly.sprite);
	    friendly.sprite.setTag(3);
		_friendlysprites.add(friendly.sprite);
		_friendlies.add(friendly);
	    // Determine where we wish to shoot the projectile to
	    /*
	    int realX = (int)(winSize.width + (projectile.getContentSize().width / 2.0f));
	    float ratio = (float)offY / (float)offX;
	    int realY = (int)((realX * ratio) + projectile.getPosition().y);
	    CGPoint realDest = CGPoint.ccp(realX, realY);
	 */
	    // Determine the length of how far we're shooting
	   /*
	    int offRealX = (int)(realX - projectile.getPosition().x);
	    int offRealY = (int)(realY - projectile.getPosition().y);
	    float length = (float)Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
	    float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
	    float realMoveDuration = length / velocity;
	 */
	    // Move projectile to actual endpoint
	   /* projectile.runAction(CCSequence.actions(
	            CCMoveTo.action(realMoveDuration, realDest),
	            CCCallFuncN.action(this, "spriteMoveFinished")));
	 */
	    return true;
	}
	
	public void updateBehavior (float dt) {
		
		for (Friendly friendly : _friendlies) {
		    CGSize winSize = CCDirector.sharedDirector().displaySize();

			if (friendly.lane.enemyCount > 0){
					CCSprite projectile = CCSprite.sprite(projectileImage);
					projectile.setPosition(friendly.sprite.getPosition());
					addChild(projectile);
					projectile.setTag(2);
					_projectiles.add(projectile);
					int realX = (int)(winSize.width);
					int realY = (int)(projectile.getPosition().y);
				    float length = (winSize.width - friendly.sprite.getPosition().x);
				    float velocity = 100.0f / 1.0f;
				    float realMoveDuration = length / velocity;
				    CGPoint realDest = CGPoint.ccp(realX, realY);
				    projectile.runAction(CCSequence.actions(
				            CCMoveTo.action(realMoveDuration, realDest),
				            CCCallFuncN.action(this, "spriteMoveFinished")));
			
				    }
		}		
	}
	
	
	
	public void update(float dt) {
		ArrayList<CCSprite> projectilesToDelete = new ArrayList<CCSprite>();
		
		for (CCSprite projectile : _projectiles) {
			
			ArrayList<CCSprite> targetsToDelete = new ArrayList<CCSprite>();
			float left1, right1, top1, bottom1;
			left1 = projectile.getPosition().x - (projectile.getContentSize().width/2.0f);
			top1 = projectile.getPosition().y + (projectile.getContentSize().height/2.0f);
			right1 = left1 + projectile.getContentSize().width;
			bottom1 = top1 - projectile.getContentSize().height;
			
			
			for (CCSprite target : _targets) {
				
				
				float left2, right2, top2, bottom2;
				left2 = target.getPosition().x - (target.getContentSize().width/2.0f);
				top2 = target.getPosition().y + (projectile.getContentSize().height/2.0f);
				right2 = left2 + target.getContentSize().width;
				bottom2 = top2 - target.getContentSize().height;
				
				if (bottom1 > top2 || top1 < bottom2 || right1 < left2 || left1 > right2) {
					//targetsToDelete.add(target);
				} else {
					targetsToDelete.add(target);

				}
			}
			
			
			for (CCSprite target : targetsToDelete) {
				_targets.remove(target);
				for (Enemy e : _enemies){
					if (target == e.sprite){
						e.lane.enemyCount--;
					}
				}
				removeChild(target, true);
			}
			
			if (targetsToDelete.size() > 0)
				projectilesToDelete.add(projectile);
		}
		
		for (CCSprite projectile : projectilesToDelete) {
			_projectiles.remove(projectile);
			removeChild(projectile, true);
		}
	}
	
	public void spriteMoveFinished(Object sender) {
		CCSprite sprite = (CCSprite) sender;
		if (sprite.getTag() == 1){
			_targets.remove(sprite);
			for (Enemy e : _enemies){
				if (sprite == e.sprite){
					e.lane.enemyCount--;
				}
			}
		}
		else if (sprite.getTag() == 2)
			_projectiles.remove(sprite);
		else if (sprite.getTag() == 3)
			_friendlies.remove(sprite);
		this.removeChild(sprite, true);
	}
	
	}
