SimpleFilter : EffectBase {
	var cutoffSpec, cutoffModSpec, server;
	/* a selection of filters that use the same types of arguments: in, freq rez, aka lowpass highpass and bandpass filters */
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_simplefilter;
	}
	
	init_simplefilter {
		cutoffSpec = [-12,12].asSpec;
		cutoffModSpec = [0, 8].asSpec;
		server = Server.default;
		winBounds = Rect.new(winBounds.left, winBounds.top, 550, 350);
		this.setGUIControls;
	}
	
	*loadSynthDef { |filter, s|
		filter = filter ? parent.filterUGens["RLPF"];
		s = s ? Server.default;
		SynthDef.new("simpleFilter", { |gain=1, bus=20, modBus=20, modAmt=0,
			mix=1, freq=440, resonance=1|
			var aIn, aScaleIn, aModIn, aFreq, aSig, aOutMix;
			aIn = In.ar(bus);
			aScaleIn = (aIn * gain).softclip;
			aModIn = In.ar(modBus) * modAmt;
			aFreq = freq + aModIn;
			aSig = SynthDef.wrap(filter, Array.fill(filter.numArgs, {0}), [aIn, aFreq, resonance]);
			aOutMix = (aIn * (mix - 1).abs) + (aSig * mix);
			ReplaceOut.ar(bus, aOutMix);
		}).load(s);
	}
	
	setGUIControls {
		addGUIControls = {
			
		};
	}

}