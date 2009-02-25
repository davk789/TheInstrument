SynthDef.new("s_monoInputChannel", { |outBus=20, channel=0|
	var aSig;
	aSig = SoundIn.ar(channel);
	Out.ar(outBus, aSig);
}).load(s);

SynthDef.new("s_stereoInputChannel", { |outBus=20, channel=0|
	var aSig;
	aSig = SoundIn.ar([channel, channel + 1]);
	Out.ar(outBus, aSig);
}).load(s);