package com.kamesuta.mc.signpic.image.meta;

import com.kamesuta.mc.bnnwidget.motion.Easings;
import com.kamesuta.mc.signpic.image.meta.RotationData.DiffRotation;
import com.kamesuta.mc.signpic.image.meta.RotationData.KeyRotation;

public abstract class MovieMeta<A extends IMotionFrame<B>, B, C extends MetaMovie<E, A>, E extends A> {
	private A base;
	private C builder = builder();
	private final Movie<A, B> movie = new Movie<A, B>(this.base = this.builder.diff(this.base));
	private boolean parsed;

	public MovieMeta() {
	}

	public boolean parse(final String src, final String key, final String value) {
		return this.parsed = this.builder.parse(src, key, value)||this.parsed;
	}

	public void next(final float time, final Easings easing) {
		if (this.parsed)
			this.movie.add(time, this.base = this.builder.diff(this.base), easing);
		this.parsed = false;
		this.builder = builder();
	}

	public abstract C builder();

	public Movie<A, B> getMovie() {
		return this.movie;
	}

	public static class SizeMovieMeta extends MovieMeta<SizeData, SizeData, ImageSize, SizeData> {
		@Override
		public ImageSize builder() {
			return new ImageSize();
		}
	}

	public static class RotationMovieMeta extends MovieMeta<KeyRotation, RotationData, ImageRotation, DiffRotation> {
		@Override
		public ImageRotation builder() {
			return new ImageRotation();
		}
	}
}
