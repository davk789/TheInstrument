MonoSequencer {
	var <>duration=0.15, <>length=0.1, <>onsetAction, <>releaseAction,
		prSequence, <>index=0, <>durIndex=0, <>lengthIndex=0, clock, isPlaying=false,
		noteSeqView, velSeqView, durationField, lengthField,
		noteView, velocityView, playButton, stopButton, pauseButton, tempoSlider;
	*new {
		^super.new.init_monosequencer;
	}
	init_monosequencer { 
		onsetAction = { |note,vel| [note,vel].postln; };
		releaseAction = { |note| note.postln; };
		clock = TempoClock.new;
		this.makeGUI;
		prSequence = noteSeqView.value.collect{ |obj,ind|
			['midi'.asSpec.map(obj), 'midi'.asSpec.map(velSeqView.value[ind])];
		};

	}
	start {
		isPlaying = true;
		clock.sched(0, {
			var param, note, vel=70;
			param = this.getNextParam;
			if(param.isArray){
				note = param[0];
		 		vel = param[1];
			}{
		 		note = param;
		 	};
			onsetAction.(note.(), vel.());
			clock.sched(this.getLength, {
				releaseAction.(note.());
				nil;
			});
		 	this.getDur;
		});
	}
	stop {
		isPlaying = false;
		index = 0;
		durIndex = 0;
		lengthIndex = 0;
	}
	pause { |val|
		if(val == 1){
			isPlaying = false;
		}{
			this.start;
		};
	}
	getNextParam {
		 var ret;
		 ret = prSequence[index.min(prSequence.size)];
		 index = (index + 1) % prSequence.size;
		 ^ret;
	}
	getDur {
		var ret;
		if(duration.isArray){
			ret = duration[durIndex.min(duration.size)];
			durIndex = (durIndex + 1) % duration.size;
		}{
			ret = duration;
		};
		if(isPlaying.not){
			ret = nil;
		};
		^ret.();
	}
	getLength {
		var ret;
		if(length.isArray){
			ret = length[lengthIndex.min(length.size)];
			lengthIndex = (lengthIndex + 1) % length.size;
		}{
			ret = length;
		};
		^ret.();
	}
	sequence_ { |seq|
		prSequence = seq;
		noteSeqView.value = prSequence.collect{ |obj,ind|
			var ret;
			if(obj.isArray){
				postln(obj);
				ret = obj[0].();
			}{
				ret = obj.();
			};
			ret = obj.();
			'midi'.asSpec.unmap(ret);
		};
		noteSeqView.indexThumbSize_(noteSeqView.bounds.width / noteSeqView.value.size);
		velSeqView.value = prSequence.collect{ |obj,ind|
			var ret;
			if(obj.isArray){
				ret = obj[1].();
			}{
				ret = 80;
			};
			'midi'.asSpec.unmap(ret);
		};
		velSeqView.indexThumbSize_(velSeqView.bounds.width / velSeqView.value.size);
	}
	sequence {
		^prSequence;
	}
	tempo_ { |val|
		clock.tempo = val;
	}
	tempo {
		^clock.tempo;
	}
	setNote { |slider|
		var noteVal, velVal;
		noteVal = 'midi'.asSpec.map(slider.currentvalue);
		velVal = 'midi'.asSpec.map(velSeqView.value[slider.index]);
		prSequence[slider.index] = [noteVal, velVal];
	}
	setVelocity { |slider|
		var noteVal, velVal;
		noteVal = 'midi'.asSpec.map(noteSeqView.value[slider.index]);
		velVal = 'midi'.asSpec.map(slider.currentvalue);
		prSequence[slider.index] = [noteVal, velVal];
	}
	setDuration { |val|
		duration = val;
	}
	setLength { |val|
		length = val;
	}
	makeGUI {
		var win;
		win = GUI.window.new("MonoSequencer", Rect.new(0, 0, 400, 500)).front;
		win.view.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
		playButton = GUI.button.new(win, Rect.new(0, 0, 90, 20))
			.states_([["play", Color.black, Color.green]])
			.action_({ |obj| this.start; });
		stopButton = GUI.button.new(win, Rect.new(0, 0, 90, 20))
			.states_([["stop", Color.red, Color.black]])
			.action_({ |obj| this.stop; });
		pauseButton = GUI.button.new(win, Rect.new(0, 0, 90, 20))
			.states_([
				["pause", Color.yellow, Color.black],
				["pause", Color.black, Color.yellow]])
			.action_({ |obj| this.pause(obj.value); });
		noteSeqView = GUI.multiSliderView.new(win, Rect.new(0, 0, 400, 150))
			.fillColor_(Color.green.alpha_(0.7))
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_('midi'.asSpec.unmap([60, 62, 64, 65, 67]))
			.isFilled_(false)
			.action_({ |obj| this.setNote(obj) });
		noteSeqView.indexThumbSize_(noteSeqView.bounds.width / noteSeqView.value.size);
		velSeqView = GUI.multiSliderView.new(win, Rect.new(0, 0, 400, 150))
			.fillColor_(Color.green.alpha_(0.7))
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_('midi'.asSpec.unmap([70, 72, 44, 95, 97]))
			.isFilled_(true)
			.action_({ |obj| this.setVelocity(obj) });
		velSeqView.indexThumbSize_(velSeqView.bounds.width / velSeqView.value.size);
		tempoSlider = EZSlider.new(
			win, 
			390 @ 20, 
			"tempo", 
			[1, 8, 'exponential'].asSpec,
			{ |obj|	this.tempo = obj.value - 1; },
			2,
			false,
			40);
		tempoSlider.labelView.stringColor_(Color.green);
		tempoSlider.numberView.background_(Color.black)
			.stringColor_(Color.green);
		durationField = GUI.textField.new(win, Rect.new(0, 0, 190, 100))
			.boxColor_(Color.black)
			.stringColor_(Color.green)
			.string_(duration.asInfString)
			.action_({ |obj| this.setDuration(obj.string.interpret) });
		lengthField = GUI.textField.new(win, Rect.new(0, 0, 190, 100))
			.boxColor_(Color.black)
			.stringColor_(Color.green)
			.string_(length.asInfString)
			.action_({ |obj| this.setLength(obj.string.interpret) });
	}
	
}

