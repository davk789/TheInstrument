SynthDef.new("monoInputChannel", { |channel=2|
	var aSig;
	aSig = SoundIn.ar(channel);
	Out.ar(aSig);
}).load(s);

SynthDef.new("stereoInputChannel", { |left=2,right=3|
	var aSig;
	aSig = SoundIn.ar([left,right]);
	Out.ar(aSig);
}).load(s);