PolyGendy : InstrumentVoice { // a challenge to myself to finish a small project
	*new {
		^super.new.init_polygendy;
	}

	init_polygendy {
		postln(this.class.asString ++ " initialized");
	}	

	*loadSythDef {
		SynthDef.new("PolyGendy", {
			|ampdist,durdist,adparam,ddparam,minfreq,maxfreq,durscale,i,add|
			Gendy1.ar(ampdist,durdist,adparam,ddparam,minfreq,maxfreq,durscale,i,add);
		});
	}
}