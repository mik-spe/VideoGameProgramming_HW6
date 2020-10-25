package com.mygdx.game;

/**
 * Mikala Spencer
 * 2020-10-24
 * This program mvoes the camera around an image, rotates, and shakes.
 */

import java.util.Scanner;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;


abstract class CameraEffect 
{
    protected OrthographicCamera cam;
    protected int duration, progress;
    protected ShapeRenderer renderer;
	protected SpriteBatch batch;
	
    public CameraEffect(OrthographicCamera cam, int duration, 
	SpriteBatch batch, ShapeRenderer renderer) 
	{
        this.cam = cam;
        this.duration = duration;
        this.batch = batch;
        this.renderer = renderer;
        progress = duration;
	}
	
	public boolean isActive() 
	{
        return (progress<duration);
	}
	
	public abstract void play();
	
	public void updateCamera() 
	{
        cam.update();
		if (renderer != null) 
		{
            renderer.setProjectionMatrix(cam.combined);
        }
		if (batch != null)
		{
            batch.setProjectionMatrix(cam.combined);
        }
	}
	
	public void start() 
	{
        progress = 0;
    }
}

class CameraShake extends CameraEffect 
{
    private float intensity;
	private int speed;
	
	public float getIntensity() 
	{
        return intensity;
	}
	
	public void setIntensity(int intensity) 
	{
		if (intensity < 0) 
		{
            this.intensity = 0;
		} else 
		{
            this.intensity = intensity;
        }
	}
	
	public int getSpeed() 
	{
        return speed;
	}
	
	public void setSpeed(int speed) 
	{
		if (speed < 0) 
		{
            speed = 0;
		} else 
		{
            this.speed = 100 - speed;
        }
	}
	
    @Override
	public boolean isActive() 
	{
        return super.isActive() && speed > 0;
	}
	
    public CameraShake(OrthographicCamera cam, int duration, SpriteBatch batch,
	ShapeRenderer renderer, int intensity, int speed) 
	{
        super(cam,duration,batch,renderer);
        setIntensity(intensity);
        setSpeed(speed);
	}
	
    @Override
	public void play() 
	{
		if (isActive()) 
		{
			if (progress % speed == 0)
			{
				// Dappens the intensity to have it close in as it shakes
				intensity = intensity * (1-((float)progress/duration));
				intensity = -intensity;
				cam.translate(10*intensity,10*intensity);
			}
			progress++;

			if (!isActive()) 
			{
				cam.translate(-intensity,-intensity);
            }
            updateCamera();
        }
	}
	
    @Override
	public void start() 
	{
		super.start();
		// Camera moves in both vertical and horizontal direction when shaking
        cam.translate(intensity,intensity);
        updateCamera();
    }
}

class InputHandler extends InputAdapter
{
	// Declare variables
	private OrthographicCamera cam;
	private SpriteBatch batch;
	private Vector3 startCam, startMouse;
	private boolean shiftHeldRotate = false;


	// Set up the camera
	public InputHandler(SpriteBatch batch, OrthographicCamera cam)
	{
		this.batch = batch;
		this.cam = cam;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		startCam = new Vector3(cam.position.x,cam.position.y,0);
		startMouse = new Vector3(screenX, screenY, 0);

		return true;
	}

	public void updateCamera()
	{
		cam.update();
		batch.setProjectionMatrix(cam.combined);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{

		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
		{
			shiftHeldRotate = true;
		}

		float diffX = screenX - startMouse.x;
		float diffY = screenY - startMouse.y;

		// If shift is held, the camera rotates the difference instead of panning
		if(shiftHeldRotate)
		{
			float diff = diffX + diffY;
			cam.rotate(diff);
		}
		else
		{
			// Prints out the coordinates of the mouse when click & dragging
			//System.out.printf("You are at (%d, %d)\n", screenX,screenY);
			
			// Pans the camera away from the image
			cam.position.x = startCam.x + diffX;
			cam.position.y = startCam.y - diffY;

			// Moves the camera to starting position when the mouse leaves the window
			if(screenX < 0 || screenY > 479 || screenX > 639 || screenY < 0)
			{
				cam.position.x = startCam.x;
				cam.position.y = startCam.y;
			}
		}

		updateCamera();

		return true;
	}
}

public class AdvancedInputHandling extends ApplicationAdapter 
{
	OrthographicCamera cam;
	SpriteBatch batch;
	Texture tex;
	TextureRegion img;
	int WIDTH;
	int HEIGHT;

	CameraShake shaker;

	 // State variables associated with location of picture
	 int imgX, imgY;
	 int imgWidth, imgHeight;
	 int imgOX, imgOY;
	 int imgAngle;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		tex = new Texture("badlogic.jpg");
		imgWidth = tex.getWidth();
		imgHeight = tex.getHeight();

		// Gives roatation abilities to the image we loaded in
		img = new TextureRegion(tex);
		imgAngle = 0;
		imgX = 0;
		imgY = 0;
		imgOX = imgWidth/2;
		imgOY = imgHeight/2;

		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
		cam = new OrthographicCamera(WIDTH,HEIGHT);

		InputHandler handler1 = new InputHandler(batch,cam);
		Gdx.input.setInputProcessor(handler1);

		cam.translate(WIDTH/2,HEIGHT/2);
		cam.update();
		batch.setProjectionMatrix(cam.combined);

		// Get user input for the speed
		Scanner sc = new Scanner(System.in);
		int speedPercent;
		// The lower the number, the slower the speed and vice versa
		System.out.print("What Speed?(0-100) ");
		speedPercent = sc.nextInt();
		shaker = new CameraShake(cam, 500, batch, null, 10, speedPercent);
	}

	// Handles all keyboard and mouse input
	public void handleInput()
	{
		boolean shiftHeld = false;

		// Update the camera
		boolean cameraNeedsUpdating = false;

		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
		{
			shiftHeld = true;
		}
		if (Gdx.input.isKeyPressed(Keys.UP))
		{
			if (shiftHeld)
			{
				// Zoom out
				cam.zoom+=0.1;
			}
			else
			{
				// Move camera up
				cam.translate(0,1);
			}
			cameraNeedsUpdating = true;
		}

		if (Gdx.input.isKeyPressed(Keys.DOWN))
		{
			if (shiftHeld)
			{
				// Zoom in
				cam.zoom+=-0.1;
			}
			else
			{
				// Move camera down
				cam.translate(0,-1);
			}
			cameraNeedsUpdating = true;
	}

	if (Gdx.input.isKeyPressed(Keys.LEFT))
		{
			if (shiftHeld)
			{
				// Rotate right
				cam.rotate(1);
			}
			else
			{
				// Move camera left
				cam.translate(-1,0);
			}
			cameraNeedsUpdating = true;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT))
		{
			if (shiftHeld)
			{
				// Rotate left
				cam.rotate(-1);
			}
			else
			{
				// Move camera right
				cam.translate(1,0);
			}
			cameraNeedsUpdating = true;
		}

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
		{
			// Exiting the game
			Gdx.app.exit();
		}

		if (Gdx.input.isKeyJustPressed(Keys.W))
		{
			// Move image up
			imgY += 5;
		}

		if (Gdx.input.isKeyJustPressed(Keys.S))
		{
			// Move image down
			imgY -= 5;
		}

		if (Gdx.input.isKeyJustPressed(Keys.A))
		{
			if (shiftHeld)
			{
				// Rotate image left
				imgAngle += 2;
			}
			else
			{
				// Move image left
				imgX -= 5;
			}
		}

		if (Gdx.input.isKeyJustPressed(Keys.D))
		{
			if (shiftHeld)
			{
				// Rotate image right
				imgAngle -= 2;
			}
			else
			{
				// Move image right
				imgX += 5;
			}
		}

		if (cameraNeedsUpdating)
		{
			updateCamera();
		} 
	}

	// Updates the camera and sets projection matrix
	public void updateCamera()
	{
		cam.update();
		batch.setProjectionMatrix(cam.combined);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		handleInput();

		if (Gdx.input.isKeyJustPressed(Keys.SPACE))
		{
			shaker.start();
		}

		shaker.play();

		batch.begin();
		batch.draw(img, imgX, imgY, imgOX, imgOY, imgWidth, imgHeight, 1f, 1f, imgAngle);
		batch.end();
	}
	
	@Override
	public void dispose () 
	{
		batch.dispose();
	}
}

