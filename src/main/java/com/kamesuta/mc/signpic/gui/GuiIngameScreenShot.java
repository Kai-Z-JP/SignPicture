package com.kamesuta.mc.signpic.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.kamesuta.mc.bnnwidget.WEvent;
import com.kamesuta.mc.bnnwidget.WFrame;
import com.kamesuta.mc.bnnwidget.WPanel;
import com.kamesuta.mc.bnnwidget.font.FontStyle;
import com.kamesuta.mc.bnnwidget.font.TrueTypeFont;
import com.kamesuta.mc.bnnwidget.font.WFontRenderer;
import com.kamesuta.mc.bnnwidget.position.Area;
import com.kamesuta.mc.bnnwidget.position.Point;
import com.kamesuta.mc.bnnwidget.position.R;
import com.kamesuta.mc.bnnwidget.render.OpenGL;
import com.kamesuta.mc.bnnwidget.render.WRenderer;
import com.kamesuta.mc.bnnwidget.render.WRenderer.BlendType;
import com.kamesuta.mc.signpic.Client;
import com.kamesuta.mc.signpic.Log;
import com.kamesuta.mc.signpic.mode.CurrentMode;
import com.kamesuta.mc.signpic.util.FileUtilitiy;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;

public class GuiIngameScreenShot extends WFrame {

	public GuiIngameScreenShot(final @Nullable GuiScreen parent) {
		super(parent);
	}

	public GuiIngameScreenShot() {
	}

	{
		setGuiPauseGame(false);
	}

	@Override
	public void onGuiClosed() {
		try {
			Mouse.setNativeCursor(null);
		} catch (final LWJGLException e) {
			Log.dev.warn("failed to change cursor", e);
		}
		super.onGuiClosed();
	}

	private static WFontRenderer font = new WFontRenderer(new TrueTypeFont(new FontStyle.Builder().build()));

	@Override
	protected void initWidget() {
		try {
			final org.lwjgl.input.Cursor cur = new org.lwjgl.input.Cursor(1, 1, 0, 0, 1, ByteBuffer.allocateDirect(1<<2).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer(), null);
			Mouse.setNativeCursor(cur);
		} catch (final LWJGLException e) {
			Log.dev.warn("failed to change cursor", e);
		}
		add(new WPanel(new R()) {
			private @Nullable Point point;
			private boolean takescreenshot;
			private boolean takingscreenshot;

			@Override
			public boolean mouseClicked(final WEvent ev, final Area pgp, final Point p, final int button) {
				this.point = p;
				return super.mouseClicked(ev, pgp, p, button);
			}

			@Override
			public void draw(final WEvent ev, final Area pgp, final Point point2, final float frame, final float popacity) {
				final Area a = getGuiPosition(pgp);
				WRenderer.startShape(BlendType.ONE_MINUS_DST_COLOR, BlendType.ZERO);
				OpenGL.glLineWidth(.5f);
				OpenGL.glColor4f(1f, 1f, 1f, 1f);
				t.startDrawing(GL11.GL_LINES);
				t.addVertex(point2.x()-4.5f, point2.y(), 0f);
				t.addVertex(point2.x()+4.5f, point2.y(), 0f);
				t.addVertex(point2.x(), point2.y()-4.5f, 0f);
				t.addVertex(point2.x(), point2.y()+4.5f, 0f);
				t.draw();
				if (this.takingscreenshot) {
					this.takingscreenshot = false;
					final Point point1 = this.point;
					if (point1!=null) {
						final Area rect = new Area(point2.x()*scaleX(), point2.y()*scaleY(), point1.x()*scaleX(), point1.y()*scaleY());
						final BufferedImage image = takeScreenshotRect((int) rect.minX(), (int) rect.minY(), (int) rect.w()-1, (int) rect.h()-1, mc.getFramebuffer());
						if (image!=null) {
							try {
								FileUtilitiy.uploadImage(image);
							} catch (final IOException e) {
								Log.notice(I18n.format("signpic.gui.notice.screenshot.ingame.capture.error", e));
							}
							if (!CurrentMode.instance.isState(CurrentMode.State.CONTINUE))
								requestClose();
						}
						this.point = null;
					}
				} else if (this.takescreenshot) {
					this.takescreenshot = false;
					this.takingscreenshot = true;
				} else if (!GuiIngameScreenShot.this.closeRequest) {
					final Point point = this.point;
					if (point!=null) {
						final Area rect = new Area(point2.x(), point2.y(), point.x(), point.y());
						WRenderer.startShape();
						OpenGL.glColor(GuiWindowScreenShot.bgcolor);
						drawAround(a, rect);
						WRenderer.startTexture();
						OpenGL.glColor(GuiWindowScreenShot.textshadowcolor);
						font.drawString(String.valueOf((int) rect.w()), rect.maxX()-5+.5f, rect.maxY()-12+.5f, 10, 3, guiScaleX(), Align.RIGHT);
						font.drawString(String.valueOf((int) rect.h()), rect.maxX()-5+.5f, rect.maxY()-8+.5f, 10, 3, guiScaleY(), Align.RIGHT);
						OpenGL.glColor(GuiWindowScreenShot.textcolor);
						font.drawString(String.valueOf((int) rect.w()), rect.maxX()-5, rect.maxY()-12, 10, 3, guiScaleX(), Align.RIGHT);
						font.drawString(String.valueOf((int) rect.h()), rect.maxX()-5, rect.maxY()-8, 10, 3, guiScaleY(), Align.RIGHT);
					} else {
						WRenderer.startShape();
						OpenGL.glColor4f(0f, 0f, 0f, .25f);
						draw(a);
					}
				}
				WRenderer.startShape();
				super.draw(ev, pgp, point2, frame, popacity);
			}

			private void drawAround(final @Nonnull Area out, final @Nonnull Area in) {
				drawAbs(out.minX(), out.minY(), out.maxX(), in.minY());
				drawAbs(out.minX(), in.minY(), in.minX(), in.maxY());
				drawAbs(in.maxX(), in.minY(), out.maxX(), in.maxY());
				drawAbs(out.minX(), in.maxY(), out.maxX(), out.maxY());
			}

			@Override
			public boolean mouseReleased(final WEvent ev, final Area pgp, final Point p, final int button) {
				if (this.point!=null)
					this.takescreenshot = true;
				return super.mouseReleased(ev, pgp, p, button);
			}
		});
	}

	private static @Nullable IntBuffer pixelBuffer;
	private static @Nullable int[] pixelValues;

	public static @Nullable BufferedImage takeScreenshotRect(final int x, final int y, final int w, final int h, final Framebuffer framebuffer) {
		try {
			final boolean fboEnabled = OpenGlHelper.isFramebufferEnabled();
			//final boolean fboEnabled = false;
			int displayWidth;
			int displayHeight;
			if (fboEnabled) {
				displayWidth = framebuffer.framebufferTextureWidth;
				displayHeight = framebuffer.framebufferTextureHeight;
			} else {
				displayWidth = Client.mc.displayWidth;
				displayHeight = Client.mc.displayHeight;
			}

			final int k = displayWidth*displayHeight;

			IntBuffer buffer = pixelBuffer;
			int[] pixel = pixelValues;
			if (buffer==null||pixel==null||buffer.capacity()<k||pixel.length<k) {
				pixelBuffer = buffer = BufferUtils.createIntBuffer(k);
				pixelValues = pixel = new int[k];
			}

			GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			buffer.clear();

			if (fboEnabled) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
				GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
			} else
				GL11.glReadPixels(x, displayHeight-(h+y+1), w, h, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

			buffer.get(pixel);
			if (fboEnabled)
				TextureUtil.func_147953_a(pixel, displayWidth, displayHeight);
			else
				TextureUtil.func_147953_a(pixel, w, h);
			BufferedImage bufferedimage = null;

			if (fboEnabled) {
				bufferedimage = new BufferedImage(w, h, 1);
				final int l = displayHeight-framebuffer.framebufferHeight;
				for (int iy = 0; iy<h; ++iy)
					for (int ix = 0; ix<w; ++ix)
						bufferedimage.setRGB(ix, iy, pixel[(iy+l+y+1)*displayWidth+ix+x]);
			} else {
				bufferedimage = new BufferedImage(w, h, 1);
				bufferedimage.setRGB(0, 0, w, h, pixel, 0, w);
			}

			return bufferedimage;
		} catch (final Exception e) {
			Log.notice(I18n.format("signpic.gui.notice.screenshot.ingame.error", e));
		}
		return null;
	}

	public static @Nullable BufferedImage takeScreenshot(final Framebuffer framebuffer) {
		return takeScreenshotRect(0, 0, Client.mc.displayWidth, Client.mc.displayHeight, framebuffer);
	}
}