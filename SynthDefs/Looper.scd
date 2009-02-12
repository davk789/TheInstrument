SynthDef.new("s_monoSampleLooper", {
// args
	arg outBus=19, bufnum=73, rate=1, start=0, end=1;
//
	var kFrames, kStart, kEnd, aPhase, aSig;
	kFrames = BufFrames.kr(bufnum);
	kStart = start * kFrames;
	kEnd = end * kFrames;
	aPhase = Phasor.ar(0, rate, kStart, kEnd);
	aSig = BufRd.ar(1, bufnum, aPhase); // mono or stereo??
	Out.ar(outBus, aSig);
}).load(s); 

SynthDef.new("s_stereoSampleLooper", {
// args
	arg outBus=19, bufnum=73, rate=1, start=0, end=1;
//
	var kFrames, kStart, kEnd, aPhase, aSig;
	kFrames = BufFrames.kr(bufnum);
	kStart = start * kFrames;
	kEnd = end * kFrames;
	aPhase = Phasor.ar(0, rate, kStart, kEnd);
	aSig = BufRd.ar(2, bufnum, aPhase); // mono or stereo??
	Out.ar(outBus, aSig);
}).load(s); 


