+ SimpleNumber {
	frand { |precision=100|
		var upscale, downscale, ratio;
		ratio = precision / this.value;
		upscale = this.value * ratio;
		downscale = upscale.rand / ratio;
		^downscale;
	}
}

