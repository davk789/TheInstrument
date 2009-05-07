MonoSequencer {
	var <>prDuration=0.15, prLength=0.1, <>onsetAction, <>releaseAction, <>doneAction,
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
			if(param.size > 1){
				note = param[0].value();
		 		vel = param[1].value();
			}{
		 		note = param.value();
		 	};
			onsetAction.value(note, vel);
			// callback?
			if((doneAction.notNil) && (index == prSequence.lastIndex)){
				{ doneAction.value(note, vel); }.defer;
			};
			clock.sched(this.getLength, {
				releaseAction.value(note);
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
		if(prDuration.isArray){
			ret = prDuration[durIndex.min(prDuration.size)];
			durIndex = (durIndex + 1) % prDuration.size;
		}{
			ret = prDuration;
		};
		if(isPlaying.not){
			ret = nil;
		};
		^ret.value();
	}
	getLength {
		var ret;
		if(prLength.isArray){
			ret = prLength[lengthIndex.min(prLength.size)];
			lengthIndex = (lengthIndex + 1) % prLength.size;
		}{
			ret = prLength;
		};
		^ret.value();
	}
	length_ { |val|
		prLength = val;
		lengthField.string = val.asInfString;
	}
	length {
		^prLength;
	}
	duration_ { |val|
		prDuration = val;
		durationField.string = prDuration.asInfString;
	}
	duration {
		^prDuration;
	}
	sequence_ { |seq|
		prSequence = seq;
		noteSeqView.value = this.sequenceToGUI(0);
		noteSeqView.indexThumbSize_(noteSeqView.bounds.width / noteSeqView.value.size);
		velSeqView.value = this.sequenceToGUI(1);
		velSeqView.indexThumbSize_(velSeqView.bounds.width / velSeqView.value.size);
	}
	sequenceToGUI { |paramFlag=0|
		^prSequence.collect{ |obj,ind|
			var ret;
			if(obj.isArray){
				ret = obj[paramFlag].value;
			}{
				if(paramFlag == 0){
					ret = obj.value;
				}{
					ret = 80;
				};
			};
			'midi'.asSpec.unmap(ret);
		};	
	}
	sequence {
		^prSequence;
	}
	setSeqVelocities { |velocities,start=0|
		velocities.do{ |obj,ind|
			prSequence[ind + start] = [prSequence[ind + start], obj];
		};
		velSeqView.value = this.sequenceToGUI(1);
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
		prDuration = val;
	}
	setLength { |val|
		prLength = val;
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
			.string_(prDuration.asInfString)
			.action_({ |obj| this.setDuration(obj.string.interpret) });
		lengthField = GUI.textField.new(win, Rect.new(0, 0, 190, 100))
			.boxColor_(Color.black)
			.stringColor_(Color.green)
			.string_(prLength.asInfString)
			.action_({ |obj| this.setLength(obj.string.interpret) });
	}
	
}

