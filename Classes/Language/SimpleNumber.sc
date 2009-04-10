+ SimpleNumber {
	frand { |precision=100|
		var upscale, downscale, ratio;
		ratio = precision / this.value.rand;
		upscale = this.value * ratio;
		downscale = upscale.rand / ratio;
		^downscale;
	}
}
