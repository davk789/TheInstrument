
SynthDef.new("s_dualWavetable", { |gate, frequency, fbMul=0, fbLag=1, xfade=0, 
							       levOne=0.7, levTwo=0.5, levThree=0.6, 
							       attTime=0.1, decTime=0.2, susTime=0.3, relTime=0.2, 
							       curve=(-1.6), lev=1, bend=1, outBus=20, buffer1=70, buffer2=71|
	var tabReadA, tabReadB, tabPhase, outPutEnv, inFreq, inScaledFreq, envel, aSig, kFreq;
	
	inFreq = LocalIn.ar(1) * fbMul;
	kFreq = (frequency * bend) + inFreq;
	tabPhase = Phasor.ar(0, kFreq * 0.023219954648526, 0, 1024);
	tabReadA = BufRd.ar(1, buffer1, tabPhase);
	tabReadB = BufRd.ar(1, buffer2, tabPhase);
		
	envel = Env.new(
		[0, levOne, levTwo, levThree, 0], 
		[attTime, susTime, decTime, relTime], 
		curve, 
		3
	);
	outPutEnv = EnvGen.ar(envel, gate, doneAction:2);
	aSig = XFade2.ar(tabReadA, tabReadB, xfade) * outPutEnv;

	LocalOut.ar(Lag2.ar(aSig, fbLag));
	Out.ar(outBus, aSig);
	
}).load(s);


SynthDef.new("s_czFakeRez", { |freq1=440, freq2=550, bend=0, fbLag=0.1, feedback=0,
							 peakA=0.6, peakB=0.3, peakC=0.6, 
							 att=0.05, dec=0.02, sus=0.1, rel=0.3,
							 lev=1, curve=1, gate, outBus=19, inBus=19|
	var aPMaster, aTrig, aPSlave, aSRead, aSig, aEnv, aLocalIn, aSRLag, 
		kFreq1, kFreq2, kMFreq;
	aEnv = EnvGen.ar(
		Env.new([0, peakA, peakB, peakC, 0] * lev, 
				[att, dec, sus, rel], curve, 3), gate, doneAction:2
	);

	kFreq1 = freq1 * 0.023219954648526; // == freq1 * 1024 / 44100
	kFreq2 = Lag.kr(freq2 * 0.023219954648526, 1) * A2K.kr(aEnv);
	aSRLag = Lag.ar(InFeedback.ar(outBus), fbLag);
	aLocalIn = aSRLag * feedback * kFreq1;
	kMFreq = kFreq1 + Lag.kr(bend * kFreq1, 0.7) + aLocalIn;
	aPMaster = Phasor.ar(0, kMFreq, 0, 1024);
	aTrig = aPMaster * -1 + 512;
	aPSlave = Phasor.ar(aTrig, kFreq2, 0, 1024);
	aSRead = BufRd.ar(1, 72, aPSlave) * ((aPMaster / 1024) * -1 + 1);

	Out.ar(outBus, aSRead.softclip * aEnv);
}).load(s);

                 