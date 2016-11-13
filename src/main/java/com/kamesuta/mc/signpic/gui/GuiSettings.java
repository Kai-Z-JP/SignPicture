package com.kamesuta.mc.signpic.gui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.kamesuta.mc.bnnwidget.WBase;
import com.kamesuta.mc.bnnwidget.WBox;
import com.kamesuta.mc.bnnwidget.WEvent;
import com.kamesuta.mc.bnnwidget.WPanel;
import com.kamesuta.mc.bnnwidget.component.MButton;
import com.kamesuta.mc.bnnwidget.component.MLabel;
import com.kamesuta.mc.bnnwidget.component.MScaledLabel;
import com.kamesuta.mc.bnnwidget.component.MSelect;
import com.kamesuta.mc.bnnwidget.component.MSelectLabel;
import com.kamesuta.mc.bnnwidget.motion.Easings;
import com.kamesuta.mc.bnnwidget.motion.MCoord;
import com.kamesuta.mc.bnnwidget.motion.Motion;
import com.kamesuta.mc.bnnwidget.position.Area;
import com.kamesuta.mc.bnnwidget.position.Coord;
import com.kamesuta.mc.bnnwidget.position.Coords;
import com.kamesuta.mc.bnnwidget.position.PCoord;
import com.kamesuta.mc.bnnwidget.position.Point;
import com.kamesuta.mc.bnnwidget.position.R;
import com.kamesuta.mc.signpic.Apis;
import com.kamesuta.mc.signpic.Apis.ImageUploaderFactory;
import com.kamesuta.mc.signpic.Apis.Setting;
import com.kamesuta.mc.signpic.Client;
import com.kamesuta.mc.signpic.Config;
import com.kamesuta.mc.signpic.gui.config.ConfigGui;
import com.kamesuta.mc.signpic.information.Informations;
import com.kamesuta.mc.signpic.render.RenderHelper;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiSettings extends WPanel {
	public static final ResourceLocation settings = new ResourceLocation("signpic", "textures/gui/settings.png");
	public static final ResourceLocation update = new ResourceLocation("signpic", "textures/gui/update.png");

	public GuiSettings(final R area) {
		super(area);
	}

	@Override
	protected void initWidget() {
		final boolean isUpdateRequired = Informations.instance.isUpdateRequired()&&Config.instance.informationUpdateGui;
		final int updatepanelHeight = isUpdateRequired ? 40 : 0;
		add(new WPanel(new R(Coord.bottom(0), Coord.height(122+updatepanelHeight))) {
			protected boolean show = true;
			protected MCoord bottom = MCoord.pbottom(0f);
			protected boolean closing;

			@Override
			protected void initWidget() {
				add(new WPanel(new R(PCoord.of(Coords.combine(Coord.pbottom(-1), Coord.bottom(2)), Coord.pbottom(0f), this.bottom))) {
					@Override
					public void update(final WEvent ev, final Area pgp, final Point p) {
						final Area a = getGuiPosition(pgp);
						final boolean b = a.pointInside(p);
						if (!closing)
							if (b) {
								if (!show) {
									bottom.stop().add(Easings.easeOutQuad.move(.7f, 1f)).start();
									mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("signpic", "gui.show"), 1.0F));
								}
								show = true;
							} else {
								if (show)
									bottom.stop().add(Easings.easeOutBounce.move(.7f, 0f)).start();
								show = false;
							}
						super.update(ev, pgp, p);
					}

					@Override
					public boolean onCloseRequest() {
						closing = true;
						bottom.stop().add(Easings.easeInBack.move(.25f, 0f)).start();
						return false;
					}

					@Override
					public boolean onClosing(final WEvent ev, final Area pgp, final Point p) {
						return bottom.isFinished();
					}

					@Override
					public boolean mouseClicked(final WEvent ev, final Area pgp, final Point p, final int button) {
						final Area a = getGuiPosition(pgp);
						return super.mouseClicked(ev, pgp, p, button)||a.pointInside(p);
					}

					@Override
					protected void initWidget() {
						add(new WPanel(new R(Coord.top(2), Coord.bottom(0))) {
							protected R line = new R(Coord.top(0), Coord.top(2));

							@Override
							public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
								final Area a = getGuiPosition(pgp);
								RenderHelper.startShape();
								glColor4f(0f, 0f, 0f, .6f);
								drawRect(a);
								glColor4f(0f/256f, 78f/256f, 155f/256f, 1f);
								drawRect(this.line.getAbsolute(a));
								super.draw(ev, pgp, p, frame, opacity);
							}

							@Override
							protected void initWidget() {
								add(new WBase(new R(Coord.top(5), Coord.left(3), Coord.width(90), Coord.height(26))) {
									@Override
									public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
										final Area a = getGuiPosition(pgp);
										texture().bindTexture(settings);
										glColor4f(1, 1, 1, 1);
										RenderHelper.startTexture();
										drawTexturedModalRect(a);
									}
								});
								add(new WPanel(new R(Coord.left(5), Coord.width(200), Coord.top(33), Coord.height(82))) {
									@Override
									public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
										final Area a = getGuiPosition(pgp);
										RenderHelper.startShape();
										glColor4f(0f, 0f, 0f, .2f);
										glLineWidth(.5f);
										draw(a, GL_LINE_LOOP);
										super.draw(ev, pgp, p, frame, opacity);
									}

									protected WBox box = new WBox(new R(Coord.left(1), Coord.right(1), Coord.top(1+15+32+1), Coord.height(32))) {
										@Override
										protected void initWidget() {
											final ImageUploaderFactory factory = Apis.instance.imageUploader.solve(Apis.instance.imageUploader.getConfig());
											Set<String> keys = null;
											if (factory!=null)
												keys = factory.keys();
											box.add(new Key(new R(), new Apis.KeySetting(keys)));
										}
									};

									@Override
									protected void initWidget() {
										float top = 1;
										add(new MLabel(new R(Coord.left(1), Coord.right(1), Coord.top(top), Coord.height(15)), I18n.format("signpic.gui.settings.api.upimage")));
										add(new WPanel(new R(Coord.left(1), Coord.right(1), Coord.top(top += 15), Coord.height(32))) {
											@Override
											public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
												final Area a = getGuiPosition(pgp);
												RenderHelper.startShape();
												glColor4f(0f, 0f, 0f, .2f);
												glLineWidth(.5f);
												draw(a, GL_LINE_LOOP);
												super.draw(ev, pgp, p, frame, opacity);
											}

											@Override
											protected void initWidget() {
												add(new MLabel(new R(Coord.left(1), Coord.right(1), Coord.top(1), Coord.height(15)), I18n.format("signpic.gui.settings.api.upimage.type")));
												add(new MSelectLabel(new R(Coord.left(1), Coord.right(1), Coord.top(16), Coord.height(15)), 15) {
													@Override
													protected void initWidget() {
														setSelector(new ListSelector() {
															{
																final List<String> settings = Lists.newArrayList("");
																settings.addAll(Apis.instance.imageUploader.getSettings());
																setList(settings);
															}
														});
														this.field.setWatermark(I18n.format("signpic.gui.settings.default"));
														setText(Apis.instance.imageUploader.getConfig());
													}

													@Override
													protected void onChanged(final String oldText, final String newText) {
														if (!StringUtils.equals(oldText, newText)) {
															if (!StringUtils.equals(newText, Apis.instance.imageUploader.getConfig()))
																Apis.instance.imageUploader.setConfig(newText);
															final ImageUploaderFactory factory = Apis.instance.imageUploader.solve(newText);
															Set<String> keys = null;
															if (factory!=null)
																keys = factory.keys();
															box.add(new Key(new R(), new Apis.KeySetting(keys)));
														}
													}
												});
											}
										});
										add(this.box);
									}
								});
								add(new MButton(new R(Coord.bottom(5+updatepanelHeight), Coord.right(5), Coord.width(60), Coord.height(15)), I18n.format("signpic.gui.settings.config")) {
									@Override
									protected boolean onClicked(final WEvent ev, final Area pgp, final Point p, final int button) {
										Client.mc.displayGuiScreen(new ConfigGui(ev.owner));
										return true;
									}
								});

								if (isUpdateRequired) {

									final MCoord state = new MCoord(0f);
									final Coord wstart = Coord.width(32);
									final Coord wend = Coord.width(37);
									final Coord hstart = Coord.height(32);
									final Coord hend = Coord.height(37);
									final Coord lstart = Coord.left(30);
									final Coord lend = Coord.left(10);
									final Coord rstart = Coord.right(30);
									final Coord rend = Coord.right(10);

									add(new WPanel(new R(Coord.bottom(0), Coord.height(updatepanelHeight))) {
										@Override
										public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity) {
											final Area a = getGuiPosition(pgp);
											glColor4f(0f, 0f, 0f, .4f);
											RenderHelper.startShape();
											drawRect(a);
											super.draw(ev, pgp, p, frame, popacity);
										}

										@Override
										protected void initWidget() {
											add(new WPanel(new R(PCoord.of(lstart, lend, state), PCoord.of(rstart, rend, state), PCoord.of(hstart, hend, state), Coord.ptop(.5f)).child(Coord.ptop(-.5f), Coord.pheight(1f))) {
												protected boolean in;

												protected MCoord rot = new MCoord(0).add(Easings.easeLinear.move(8.04f/4f, 1f)).setLoop(true).start();
												protected float orot = 0f;

												@Override
												public void update(final WEvent ev, final Area pgp, final Point p) {
													final Area a = getGuiPosition(pgp);
													if (a.pointInside(p)) {
														if (!this.in) {
															state.stop().add(Easings.easeOutCubic.move(1f, 1f)).start();
															this.orot += this.rot.get();
															this.rot.stopFirst().reset().add(Easings.easeLinear.move(8.04f/4f, 1f));
														}
														this.in = true;
													} else {
														if (this.in) {
															state.stop().add(Easings.easeOutCubic.move(1f, 0f)).start();
															this.orot += this.rot.get();
															this.rot.stopFirst().reset().add(Easings.easeOutSine.move(1.5f, .5f)).add(Motion.of(Motion.move(0), Easings.easeInOutSine.move(2.87f, 1f), Motion.blank(0.58f)).setLoop(true)).add(Motion.move(0)).add(Easings.easeInOutElastic.move(3f, 1f));
														}
														this.in = false;
													}
													super.update(ev, pgp, p);
												}

												@Override
												public boolean mouseClicked(final WEvent ev, final Area pgp, final Point p, final int button) {
													final Area a = getGuiPosition(pgp);
													if (a.pointInside(p))
														Informations.instance.runUpdate();
													return true;
												}

												@Override
												protected void initWidget() {
													add(new WBase(new R(PCoord.of(wstart, wend, state), Coord.pleft(.5f)).child(Coord.pleft(-.5f))) {
														@Override
														public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
															final Area a = getGuiPosition(pgp);
															texture().bindTexture(update);
															// glColor4f(1, 1, 1, 1);
															final float f = state.get();
															glColor4f(256f*(1-f)/256f+144*f/256f, 256f*(1-f)/256f+191*f/256f, 256f*(1-f)/256f+48*f/256f, 1f);
															RenderHelper.startTexture();
															glPushMatrix();
															glTranslatef(a.x1()+a.w()/2, a.y1()+a.h()/2, 0f);
															glRotatef((orot+rot.get())*360, 0, 0, 1);
															glTranslatef(-a.x1()-a.w()/2, -a.y1()-a.h()/2, 0f);
															drawTexturedModalRect(a);
															glPopMatrix();
														}
													});
													final MCoord o = new MCoord(0).add(Easings.easeLinear.move(.8f, 1f)).add(Motion.blank(2f)).add(Easings.easeLinear.move(.8f, 0f)).add(Motion.blank(2f)).setLoop(true).start();
													final String message = I18n.format("signpic.gui.update.message");
													final String update = Informations.instance.getUpdateMessage();
													add(new MScaledLabel(new R(Coord.pheight(.4f), Coord.ptop(.5f)).child(Coord.ptop(-.5f), Coord.pheight(1f)), message) {
														@Override
														protected void initOpacity() {
															super.setOpacity(o);
														}

														@Override
														public int getColor() {
															final float f = state.get();
															return new Color(256f*(1-f)/256f+144*f/256f, 256f*(1-f)/256f+191*f/256f, 256f*(1-f)/256f+48*f/256f).getRGB();
														}

														@Override
														public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity) {
															glPushMatrix();
															glTranslatef(0, 0, 10f);
															super.draw(ev, pgp, p, frame, popacity);
															glPopMatrix();
														}
													}.setShadow(true));
													add(new MScaledLabel(new R(Coord.pheight(.4f), Coord.ptop(.5f)).child(Coord.ptop(-.5f), Coord.pheight(1f)), !StringUtils.isEmpty(update) ? I18n.format("signpic.gui.update.message.changelog", update) : message) {
														@Override
														protected void initOpacity() {
															super.setOpacity(PCoord.of(Coord.ptop(1f), Coord.ptop(0f), o));
														}

														@Override
														public int getColor() {
															final float f = state.get();
															return new Color(256f*(1-f)/256f+144*f/256f, 256f*(1-f)/256f+191*f/256f, 256f*(1-f)/256f+48*f/256f).getRGB();
														}

														@Override
														public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float popacity) {
															glPushMatrix();
															glTranslatef(0, 0, 10f);
															super.draw(ev, pgp, p, frame, popacity);
															glPopMatrix();
														}
													}.setShadow(true));
												}
											});
										}
									});
								}
							}
						});
					}
				});
			}
		});
	}

	class Key extends WPanel {
		protected Setting setting;

		public Key(final R position, final Setting setting) {
			super(position);
			this.setting = setting;
		}

		@Override
		public void draw(final WEvent ev, final Area pgp, final Point p, final float frame, final float opacity) {
			final Area a = getGuiPosition(pgp);
			RenderHelper.startShape();
			glColor4f(0f, 0f, 0f, .2f);
			glLineWidth(.5f);
			draw(a, GL_LINE_LOOP);
			super.draw(ev, pgp, p, frame, opacity);
		}

		@Override
		protected void initWidget() {
			add(new MLabel(new R(Coord.left(1), Coord.right(1), Coord.top(1), Coord.height(15)), I18n.format("signpic.gui.settings.api.upimage.key")));
			add(new MSelect(new R(Coord.left(1), Coord.right(1), Coord.top(16), Coord.height(15)), 15) {
				@Override
				protected void initWidget() {
					setSelector(new ListSelector() {
						{
							final List<String> settings = Lists.newArrayList("");
							if (Key.this.setting!=null)
								settings.addAll(Key.this.setting.getSettings());
							setList(settings);
						}
					});
					this.field.setWatermark(I18n.format("signpic.gui.settings.default"));
					this.field.setMaxStringLength(256);
					setText(Key.this.setting.getConfig());
				}

				@Override
				protected void onChanged(final String oldText, final String newText) {
					if (!StringUtils.equals(oldText, newText)&&!StringUtils.equals(newText, Key.this.setting.getConfig()))
						Key.this.setting.setConfig(newText);
				}
			});
		}
	}
}