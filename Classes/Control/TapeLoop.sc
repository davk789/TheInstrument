TapeLoop {
	classvar bufnumInd;
	var <>bufnum, <>prChannels=2, server, loopLength;
	*new { |parent, bounds, numChannels|
		^super.new.init_tapeloop(parent, bounds, numChannels);
	}
	init_tapeloop { |parent, bounds, numChannels|
		server = Server.local;
		prChannels = numChannels ? prChannels;
		this.loadSynthDef;
		if(bounds.notNil){
			this.makeGUI(parent, bounds);	
		};
	}
	channels_ { |val|
		// stop the synth, free the buffer, set prChannels, reload SynthDef, reload buffer, start synth
		prChannels = val;
	}
	channels {
		^prChannels;
	}
	makeGUI { |parent,bounds|
		var win;
		if(parent.notNil){
			win = GUI.window.front("Tape Loop", Rect.new(500.rand, 500.rand, 400, 200)).front;
			win.view.decorator = FlowLayout(win.view.bounds);
		};
	}
	loadSynthDef { 
		SynthDef.new("s_tapeLoop", {
		// args
			arg outBus=19, bufnum=73, rate=1, start=0, dur=1,
				inBus=20, recLevel=1, oldLevel=0;
		//
			var kFrames, kStart, kEnd, aPhase, aRead, aWrite, aIn;
			kFrames = BufFrames.kr(bufnum);
			kStart = start * kFrames;
			kEnd = ((start + dur) * kFrames).min(kFrames);
			
			aPhase = Phasor.ar(0, rate, kStart, kEnd);
			
			aRead = BufRd.ar(prChannels, bufnum, aPhase);
			Out.ar(outBus, aRead);

			aIn = (InFeedback.ar(inBus, prChannels).softclip * recLevel) + (aRead * oldLevel);
			aWrite = BufWr.ar(aIn, bufnum, aPhase, 1);

		}).load(server);
	}
}