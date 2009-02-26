/*** DONE ***/
SynthDef.new("monoMixerChannel", { |pan=0, lev=1, gain=1, inBus=22, outBus=20|
    var aSig, aIn;
	aIn = (In.ar(inBus, 1) * gain).softclip;
    aSig = Pan2.ar(aIn, pan, lev);
    Out.ar(outBus, aSig);
	Out.ar(3, aSig); // temporary hack for modular synth
}).load(s);
SynthDef.new("stereoMixerChannel", { |pan=0, lev=1, gain=1, inBus=22, outBus=20|
    var aSig, aIn;
	aIn = (In.ar(inBus, 2) * gain).softclip;
    aSig = Balance2.ar(aIn[0], aIn[1], pan, lev);
    Out.ar(outBus, aSig);
}).load(s);
// there also should be a stereo version of this
SynthDef.new("fx_monoDelay", { |bus=20, mix=1, delayTime=0.1, feedback=0, 
								modBus=20, modAmt=0, modLag=1|
    var aIn, aDelay, aDelayIn, aLocalIn, aOutMix, aModIn;
    aModIn = Lag.ar(InFeedback.ar(modBus) * modAmt, modLag);
	aLocalIn = LocalIn.ar(1);
    aIn = In.ar(bus);
	aDelayIn = (aIn * (feedback - 1).abs) + (aLocalIn * feedback);
    aDelay = DelayC.ar(aDelayIn, 10, delayTime + aModIn);
	LocalOut.ar(aDelay);
	aOutMix = (aIn * (mix - 1).abs) + (aDelay * mix);
    ReplaceOut.ar(bus, aOutMix.softclip);
}).load(s);
SynthDef.new("fx_distortion", { |bus, buffer=73, gain=1, mix=(-1)|
    var aIn, aShape, aSig;
    aIn = In.ar(bus) * gain;
    aShape = Shaper.ar(buffer, aIn);
	aSig = XFade2.ar(aShape, aIn, mix);
    ReplaceOut.ar(bus, aSig);
}).load(s);
/* TO DO */
SynthDef.new("fx_ringMod", { |bus, mix=1, freq=440, amount=0|
	var aSig;
	aSig = In.ar(bus) * SinOsc.ar(freq, 0, amount);
	ReplaceOut.ar(bus, aSig);
}).load(s);

SynthDef.new("fx_compressor", { |bus=20, mix=1, 
	control, threshold=0.6, slopBelow=1, slopeAbove=0.2, 
	clampTime=0.005, relaxTime=0.01|
    var aSig;
    aIn = In.ar(bus);
    aSig = Compander.ar(aIn, control, thresh, slopeBelow, slopeAbove);
    ReplaceOut(bus, aSig);
}).load(s);
SynthDef.new("fx_eq", { |bus, mix|
    var aSig;
    aSig = 0;// blabla filters here;
	ReplaceOut.ar(bus, aSig);
}).load(s);

SynthDef.new("fx_pitchShift", { |bus, mix|
	var aSig;
	aSig = 0; // crossfading delay pitch shift here
	ReplaceOut.ar(bus, aSig);
}).load(s);
             
