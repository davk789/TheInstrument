
SynthDef.new("s_dualWavetable", { |gate, outBus=20, bend=1, curve=(-1.6), lev=1,
							       peakA=0.7, peakB=0.5, peakC=0.6, 
							       att=0.1, dec=0.2, sus=0.3, rel=0.2, 
								   fbMul=0, fbLag=1, 
								   frequency, xfade=0, 
								   buffer1=70, buffer2=71|
	var tabReadA, tabReadB, tabPhase, kEnv, inFreq, inScaledFreq, aSig, aFreq;
	
	inFreq = LocalIn.ar(1) * fbMul;
	aFreq = (frequency * bend) + inFreq;
	tabPhase = Phasor.ar(0, aFreq * 0.023219954648526, 0, 1024);
	tabReadA = BufRd.ar(1, buffer1, tabPhase);
	tabReadB = BufRd.ar(1, buffer2, tabPhase);
	
	kEnv = EnvGen.kr(
		Env.new([0, peakA, peakB, peakC, 0], [att, sus, dec, rel], curve, 3);,
		gate, 
		doneAction:2
	);
	aSig = XFade2.ar(tabReadA, tabReadB, xfade) * kEnv;

	LocalOut.ar(Lag2.ar(aSig, fbLag));
	Out.ar(outBus, aSig);
	
}).load(s);

SynthDef.new("s_czFakeRez", { |gate, outBus=19, bend=0, lev=1, curve=(-2)
							   peakA=0.6, peakB=0.3, peakC=0.6, 
							   att=0.05, dec=0.02, sus=0.1, rel=0.3,
							   fbLag=0.1, feedback=0,
							   inBus=19, freq1=440, freq2=1.1|
	var aPMaster, aTrig, aPSlave, aSRead, aSig, aLocalIn, aSRLag, 
		kFreq1, kFreq2, kMFreq, kEnv;
	kEnv = EnvGen.kr(
		Env.new([0, peakA, peakB, peakC, 0] * lev, 
				[att, dec, sus, rel], curve, 3), gate, doneAction:2
	);

	kFreq1 = freq1 * 0.023219954648526; // == freq1 * 1024 / 44100
	kFreq2 = Lag.kr(freq1 * freq2 * 0.023219954648526, 1) * kEnv;
	aSRLag = Lag.ar(InFeedback.ar(outBus), fbLag);
	aLocalIn = aSRLag * feedback * kFreq1;
	kMFreq = kFreq1 + Lag.kr(bend * kFreq1, 0.7) + aLocalIn;
	aPMaster = Phasor.ar(0, kMFreq, 0, 1024);
	aTrig = aPMaster * -1 + 512;
	aPSlave = Phasor.ar(aTrig, kFreq2, 0, 1024);
	aSRead = BufRd.ar(1, 72, aPSlave) * ((aPMaster / 1024) * -1 + 1);

	Out.ar(outBus, aSRead.softclip * kEnv);
}).load(s);
                   