WavetableSynthManager : Model {
	var environment, server;
	*new { |mod,env|
		^super.newCopyArgs(mod).init_wavetablesynthmanager(env);
	}
	
	init_wavetablesynthmanager { |env|
		server = Server.default;
		environment = env;
		postln(this.class.asString ++ " initialized");
	}

	loadSynthDef { |filter|
		filter = filter ? environment.filterUGens["MoogFF"];
		SynthDef.new("s_dualWavetableRLPF", { 
			|gate, outBus=20, 
				curve=(-1.6), lev=1,
				peakA=0.7, peakB=0.5, peakC=0.6, 
				att=0.1, dec=0.2, sus=0.3, rel=0.2, 
				fbMul=0, fbLag=1, fmAmt=0,
				freq1=440, freq2=0, trigMode=0, bend=1, xfade=0, resonance=1,
				bufferA=70, bufferB=71, 
				fmEnvFlag=0, freq2EnvFlag=0, fbMulEnvFlag=0, envScale=1,
				cutoff=0, cutoffMod=0, cutoffFlag=0, cutoffModFlag=0, modSource=0|
			var aTabReadA, aTabReadB, aTabPhase1, aTabPhase2, aEnv, aInFreq, aSig, aFreq1, aFreq2, aTrig, aConstant1, aInvSync, lSyncCoefs, aSyncCoef, asFreq2Env, kFreq1, kFreq2, aFM2, asInEnv, aFilt, aLPFreq, asLPFModSources, aLPFreqMod, asCutoff, asCutoffFreqEnv, asCutoffModEnv;
			 
			aEnv = EnvGen.ar(Env.new([0, peakA, peakB, peakC, 0], [att, sus, dec, rel], curve, 3), gate, lev, 0, envScale, 2);
			aConstant1 = DC.ar(1);
		
			asInEnv = Select.ar(fbMulEnvFlag, [aConstant1, aEnv]);
			aInFreq = Lag2.ar(InFeedback.ar(outBus) * fbMul * asInEnv, fbLag);
			
			kFreq1 = freq1 * bend;
			aFreq1 = kFreq1 + aInFreq;
			kFreq2 = freq2 * kFreq1;
			asFreq2Env = Select.ar(freq2EnvFlag, [aConstant1, aEnv]);
			aFreq2 = (kFreq2 * asFreq2Env) +  kFreq1;
		 
			aTabPhase1 = Phasor.ar(0, aFreq1 * 0.023219954648526, 0, 1024);
			aTabReadA = BufRd.ar(1, bufferA, aTabPhase1);
		
			aTrig = (aTabPhase1 * -1  + 512) * trigMode;
			aFM2 = aTabReadA * fmAmt * freq1;
			aTabPhase2 = Phasor.ar(aTrig, (aFreq2 + aFM2) * 0.023219954648526, 0, 1024);
		
			aInvSync = ((aTabPhase1 / 1024) - 1).abs;
			lSyncCoefs = [aConstant1, aInvSync, aConstant1];
			aSyncCoef = Select.ar(trigMode, lSyncCoefs);
		
			aTabReadB = BufRd.ar(1, bufferB, aTabPhase2) * aSyncCoef;
		   
		 	aSig = XFade2.ar(aTabReadA, aTabReadB, xfade);
		 	
		 	asCutoffFreqEnv = Select.ar(cutoffFlag, [aConstant1, aEnv]);
		 	asCutoffModEnv = Select.ar(cutoffModFlag, [aConstant1, aEnv]);
			asLPFModSources = Select.ar(modSource, [aTabReadA, aTabReadB]);
			
			aLPFreqMod = freq1 * asLPFModSources * cutoffMod * asCutoffModEnv;
			aLPFreq = (freq1 + (cutoff * freq1 * asCutoffFreqEnv)) + aLPFreqMod;
			
			aFilt = SynthDef.wrap(filter, [0, 0, 0], [aSig.softclip, aLPFreq, resonance]);
		 
			Out.ar(outBus, aFilt * aEnv);
		}, [nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, 1, nil, 1, 1, 1, nil, 1, 1, 1, nil, nil, nil, nil, nil, nil, 1, 1]).load(server);
		
		/*		SynthDef.new("wavetableSynthLFO", {
			|freq, waveformType, outBus|
			var aCub, aTri, aSig;
			
			aCub = LFCub.ar(0.2, 0, 1);			
			aCub = LFTri.ar(0.2, 0, 1);
			
			aSig = Select.ar(waveformType, [aCub, aTri]);

			Out.ar(outBus, aSig);
			
		}).load(s);
		*/
	
	
	}

 
}