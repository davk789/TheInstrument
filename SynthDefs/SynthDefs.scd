
SynthDef.new("sineSumEnvelope", { |gate, frequency, freqBus, freqMul=0, xfade=0, 
							       levelsOne=0, levelsTwo=1, levelsThree=0.9, levelsFour=0.95, levelsFive=0, 
							       timesOne=0.1, timesTwo=0.2, timesThree=0.3, timesFour=0.2, 
							       curve=(-1.6), lev=1, bend=1, outBus=20|
	var tabReadA, tabReadB, tabPhase, outPutEnv, inFreq, inScaledFreq, envel;
	
	inFreq = InFeedback.ar(freqBus).clip2(1) * freqMul;
	
	tabPhase = Phasor.ar(0, (frequency * bend) + inFreq, 0, 1024);
	tabReadA = BufRd.ar(1, 70, tabPhase);
	tabReadB = BufRd.ar(1, 71, tabPhase);
		
	envel = Env.new([levelsOne, levelsTwo, levelsThree, levelsFour, levelsFive], [timesOne, timesTwo, timesThree, timesFour], curve, 3);
	outPutEnv = EnvGen.ar(envel, gate, doneAction:2);
	
	Out.ar(outBus, (XFade2.ar(tabReadA, tabReadB, xfade) * outPutEnv).softclip);
	
}, [0.1, 0, 0.1, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, 0.2]).load(s);


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
/*
SynthDef.new("o_czFakeRez", { |inBus=19, pan=0, lev=1, outBus=0|
	var sig;
	sig = In.ar(inBus).softclip;
	Out.ar(outBus, Pan2.ar(sig, pan, lev));
}).load(s);


SynthDef.new("s_dualOsc", { |freq=440, xFade=0,
							 peakA=0.6, peakB=0.3, peakC=0.6, 
							 att=0.05, dec=0.02, sus=0.1, rel=0.3,
							 lev=1, curve=1, gate, outBus=18|
	var aPhase, aWaveA, aWaveB, aEnv, aSig;
	aPhase = Phasor.ar(0, freq * 0.023219954648526, 0, 1024); // multiply freq by buffersize / samplerate 
	aWaveA = BufRd.ar(1, 70, aPhase);
	aWaveB = BufRd.ar(1, 71, aPhase);

	aEnv = EnvGen.ar(
		Env.new([0, peakA, peakB, peakC, 0] * lev, 
				[att, dec, sus, rel], curve, 3), gate, doneAction:2
	);

	aSig = XFade2.ar(aWaveA, aWaveB, xFade);
	Out.ar(outBus, (aEnv * aSig.softclip));
	
}).load(s);
*/
 