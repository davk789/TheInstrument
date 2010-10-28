postln("loading SynthDefs");
// Drums ~~~~~~~~~~
SynthDef.new("x_osc", { 
	|att=0.001, rel=0.5, lev=1, curve=(-8), freq=80, 
		modPhase=0, modFreq=(-1), modAmt=0, drive=0, trig=1, outBus=15| 
    var uMod, uOsc, uEnv, uDrive, kModFreq, kModAmt;

    kModFreq = (modFreq * 0.25) / (att + rel);
    kModAmt = modAmt * freq;  // mod amount range == an upper limit number > 1 and its reciprocal

    uMod = SinOsc.ar(kModFreq, modPhase, kModAmt, freq);
    uOsc = SinOsc.ar(uMod, 0);// + SinOsc.ar(uMod * 1.3061224489795, 0); // stacked septimal whole tones -- maj 3rd
    uDrive = (uOsc * (drive + 1)).distort;
    uEnv = EnvGen.ar(Env.perc(att, rel, lev, curve), trig, doneAction:2);
    Out.ar(outBus, uDrive * uEnv);

}).load(s);

SynthDef.new("x_gray", { 
	|att=0.001, rel=0.5, lev=1, curve=(-8), 
		trig=1, outBus=10|
	var aNoise, aEnv;
	aNoise = GrayNoise.ar;
	aEnv = EnvGen.ar(Env.perc(att, rel, lev, curve), trig, doneAction:2);
	Out.ar(outBus, RLPF.ar(aNoise * aEnv, 200, 1));
}).load(s);

SynthDef.new("x_crackle", { 
	|att=0.001, rel=1.5, lev=1, curve=(-4), 
		trig=1, outBus=11, crackle=1.5, gain=1|
	var aNoise, aEnv;
	aNoise = Crackle.ar(crackle);
	aEnv = EnvGen.ar(Env.perc(att, rel, lev, curve), trig, doneAction:2);
	Out.ar(outBus, (aNoise * aEnv).softclip);//RLPF.ar(aNoise * aEnv, 200, 1));
}).load(s);

SynthDef.new("x_clip", { 
	|att=0.001, rel=0.5, lev=1, curve=(-8), 
		trig=1, outBus=10|
	var aNoise, aEnv;
	aNoise = ClipNoise.ar;
	aEnv = EnvGen.ar(Env.perc(att, rel, lev * 0.25, curve), trig, doneAction:2);
	Out.ar(outBus, RLPF.ar(aNoise * aEnv, 200, 1));
}).load(s);

SynthDef.new("x_whiteSnare", { 
	|outBus=10, freq=1200, gain=1, rez=2, trig=1, 
		att=0.011, rel=0.5, lev=1, curve=(-10)|
	var aNoise, aLo, aHi, aSig, kRez, aEnv;
	kRez = rez.reciprocal; 
	aNoise = ClipNoise.ar(gain);
	aLo = RLPF.ar(aNoise, freq, rez);
	aHi = RHPF.ar(aLo, freq, rez);
	aEnv = EnvGen.ar(Env.perc(att, rel, lev, curve), trig, doneAction:2);
	aSig = aLo + aHi;
	Out.ar(outBus, aSig * aEnv);
}).load(s);

// Resonators ~~~~~~~~~~
SynthDef.new("r_formlet", { |freq=1600, attTime=0.01, decTime=0.1,
						     outBus=0, inBus=11, lev=1|
	var aRez;
	aRez = Formlet.ar(In.ar(inBus), freq, attTime, decTime);
	Out.ar(outBus, Pan2.ar(aRez) * lev);
}).load(s);

SynthDef.new("r_lpf", { |freq=1600, res=10,
						 outBus=0, inBus=12, lev=1, gain=1|
	var aRez, aIn;
	aIn = (In.ar(inBus) * gain).softclip;
	aRez = RLPF.ar(aIn, freq, 1 / res);
	Out.ar(outBus, Pan2.ar(aRez) * lev);
}).load(s);	

SynthDef.new("r_hpf", { |freq=1600, res=10,
						 outBus=0, inBus=13, lev=1, gain=1|
	var aRez, aIn;
	aIn = (In.ar(inBus) * gain).softclip;
	aRez = RHPF.ar(aIn, freq, 1 / res);
	Out.ar(outBus, Pan2.ar(aRez) * lev);
}).load(s);	

SynthDef.new("r_klank", { |outBus=0, inBus=10, lev=1, 
						   f1=80,f2=90,f3=145,
						   r1=5,r2=4,r3=6,
						   a1=0.7,a2=0.8,a3=0.4|
	var aRez;
	aRez = DynKlank.ar(
		`[[f1, f2, f3],
		  [a1, a2, a3],
		  [r1, r2, r3]], 
		Limiter.ar(In.ar(inBus), 0.5, 0.02).softclip
	);
	Out.ar(outBus, Pan2.ar(aRez) * lev);
}).load(s);


