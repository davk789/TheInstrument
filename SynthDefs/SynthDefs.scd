
SynthDef.new("s_dualWavetable", { |gate, outBus=20, 
								curve=(-1.6), lev=1,
                                peakA=0.7, peakB=0.5, peakC=0.6, 
                                att=0.1, dec=0.2, sus=0.3, rel=0.2, 
                                fbMul=0, fbLag=1, 
                                freq1=440, freq2=440, trigMode=0, bend=1, xfade=0, 
                                buffer1=70, buffer2=71|
    var aTabReadA, aTabReadB, aTabPhase1, aTabPhase2, aEnv, aInFreq, aSig, aFreq1, aFreq2, aTrig, aConstant1, aInvSync, lSyncCoefs, aSyncCoef, dTrigMode, aTrigMode;
     
    aInFreq = LocalIn.ar(1) * fbMul;

    aFreq1 = (freq1 * bend) + aInFreq;
    aFreq2 = (freq2 * bend) + (aFreq1 * fm);
 
    aTabPhase1 = Phasor.ar(0, aFreq1 * 0.023219954648526, 0, 1024);

    dTrigMode = Dswitch.new([0, 1, 1], trigMode);
    aTrigMode = Demand.ar(aTabPhase1, 0, dTrigMode);

    aTrig = Trig1.ar(aTabPhase1 * aTrigMode, 1 / 44100);
    aTabPhase2 = Phasor.ar(aTrig, aFreq2 * 0.023219954648526, 0, 1024);

    aConstant1 = DC.ar(1);
    aInvSync = ((aTabPhase1 / 1024) - 1).abs;
    lSyncCoefs = [aConstant1, aConstant1, aInvSync];
    aSyncCoef = Select.ar(trigMode, lSyncCoefs);

    aTabReadA = BufRd.ar(1, buffer1, aTabPhase1);
    aTabReadB = BufRd.ar(1, buffer2 * aSyncCoef, aTabPhase2);
   
 
    aEnv = EnvGen.ar(Env.new([0, peakA, peakB, peakC, 0], [att, sus, dec, rel], curve, 3), gate, lev, doneAction:2);
    aSig = XFade2.ar(aTabReadA, aTabReadB, xfade) * aEnv;
 
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

                   