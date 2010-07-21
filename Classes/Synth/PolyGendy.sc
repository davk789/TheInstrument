PolyGendy : InstrumentVoice { // a challenge to myself to finish a small project
	*new { |par|
		^super.new(par).init_polygendy;
	}

	init_polygendy {
		activeNotes = Dictionary.new;
		this.initializeMIDI;
		this.makeGUI;
		postln(this.class.asString ++ " initialized");
	}

	initializeMIDI {
		noteOnFunction = { |src,chan,num,vel|
			this.addActiveNote(num, server.nextNodeID);
			[src,chan,num,vel].postln;
		};

		noteOffFunction = { |src,chan,num,vel|
			this.removeActiveNote(num);
			[src,chan,num,vel].postln;
		};
 
		ccFunction = { |src,chan,num,val|
			[src,chan,num,val].postln;
		};

		bendFunction = { |src,chan,val|
			[src,chan,val].postln;
		};

		afterTouchFunction = { |src,chan,val|
			[src,chan,val].postln;
		};

	}

	*loadSythDef {
		SynthDef.new("PolyGendy", {
			|ampdist,durdist,adparam,ddparam,minfreq,maxfreq,durscale,initcps,mul,add|
			Gendy1.ar(
				ampdist, /*Choice of probability distribution for the next perturbation of the amplitude of a control point. 
					The distributions are (adapted from the GENDYN program in Formalized Music):
					0- LINEAR
					1- CAUCHY
					2- LOGIST
					3- HYPERBCOS
					4- ARCSINE
					5- EXPON
					6- SINUS
				*/
				durdist, //Choice of distribution for the perturbation of the current inter control point duration
						 // presumably the same options as the ampdist arg
				adparam, /*A parameter for the shape of the amplitude probability distribution, requires values in the range 0.0001 to 1 						   (there are safety checks in the code so don't worry too much if you want to modulate!)*/
				ddparam, // A parameter for the shape of the duration probability distribution, requires values in the range 0.0001 to 1
				minfreq, //Minimum allowed frequency of oscillation for the Gendy1 oscillator, so gives the largest period the duration is allowed to take on. 
				maxfreq, //Maximum allowed frequency of oscillation for the Gendy1 oscillator, so gives the smallest period the duration is allowed to take on. 
				durscale, //Normally 0.0 to 1.0, multiplier for the distribution's delta value for duration. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of duration.
				initcps, /*initCPs- Initialise the number of control points in the memory. Xenakis specifies 12. There would be this number of control points per cycle of the oscillator, though the oscillator's period will constantly change due to the duration distribution.  */
				mul,
				add);
		}).load(server);
	}
	
	makeGUI {
		var win;
		win = GUI.window.new("PolyGendy", Rect.new(500.rand, 500.rand, 600, 200)).front;
		
	}

}