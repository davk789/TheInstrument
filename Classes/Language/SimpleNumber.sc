+ SimpleNumber {
	frand { |precision=100|
		var upscale, downscale, ratio;
		ratio = precision / this.value;
		upscale = this.value * ratio;
		downscale = upscale.rand / ratio;
		^downscale;
	}
	increment { |amt=1|
		^this.value + amt;
	}
	decrement { |amt=1|
		^this.value - amt;
	}
}

