EventLooperChannel {
	classvar <>root;
	var <>clock, <totalTime, index, nextTime, <eventValue, lastEvent, iterator, >action,
		waitTime=0, firstEvent=true, <>isRecording=false, <eventCollection, <metaSeq,
		<>seqMenu, <>playButton, <>recordButton, incseqButtonDown, incseqButtonUp, presetListMenu, 
		presetListSubfolder, gate=false,
		currSeq=0, cTempo=1, sep, <>synthGroup;
	*new { |name, win, group|
		^super.new.init_eventLooperChannel(name, win, group);
	}
	init_eventLooperChannel { |name,win,group|
		sep = Platform.pathSeparator;
		presetListSubfolder = "trig";
		root = Platform.userAppSupportDir ++ sep ++ "EventLooperGroups";
		totalTime = 36; // this variable needs to be more accessible
		index = 0;
		nextTime = [totalTime];
		eventValue = [nil];
		eventCollection = [];
		/*action = { |id,beat|
		    postln("waiting " ++ waitTime ++ " beats");
		};*/
		iterator = this.getIterator;
		this.makeGUI(name, win);
		this.populatePresetListMenu;
		metaSeq = MetaSequence.new(eventValue.size, win);
		/// existence of a group argument determines whether or not to allow gating
		if(group.notNil){
			synthGroup = group;
			presetListSubfolder = "gate";
			gate = true;
		};

	}

	getIterator {
		^{
			var nextSeq;
			lastEvent = clock.beats;
			index = (index + 1) % nextTime.size;
			metaSeq.sIndex = index;
			waitTime = nextTime[index];
			// still don't know why i have to check for firstEvent.not but if it works...
			if(eventValue[index].notNil && firstEvent.not){
				action.value(eventValue[index],index);
				//postln("waiting " ++ waitTime ++ " beats");
			};
			// metaSequence iterator
			nextSeq = metaSeq.next;
			if(nextSeq != -1){
				this.load((nextSeq + currSeq) % eventCollection.size);
				postln("load this sequence: ( " ++ nextSeq ++ " + " ++ currSeq ++ " ) % "++eventCollection.size);
			};
			//
			waitTime;
		};
	}

	tempo_ { |val|
		cTempo = val;
		clock.tempo = cTempo;
	}

	tempo {
		^cTempo;
	}

	startRecording {
		isRecording = true;
		if(iterator.isNil){
			this.start;
		};
	}

	stopRecording {
		isRecording = false;
	}

	start {
		index = 0;
		clock = TempoClock.new(cTempo);
		iterator.value;
		clock.schedAbs(waitTime + clock.beats, iterator);
		firstEvent = false;
	}

	stop {
		if(gate){ this.freeAllVoices; };
		isRecording = false;
		//clock.tempo = 0;
		clock.stop;
	}

	addEvent { |id|
		var lastDelta, currDelta, totalDelta, currBeat, kludge;
		kludge = eventCollection.asInfString;
		if(isRecording){
			if(firstEvent){
				eventValue[0] = id;
				this.start;
			}{
				totalDelta = nextTime[index];
				currBeat =  clock.beats;
				lastDelta = currBeat - lastEvent;
				currDelta = totalDelta - lastDelta;
				nextTime.put(index, currDelta);
				nextTime = nextTime.insert(index, lastDelta);
				lastEvent = clock.beats;
				index = (index + 1) % nextTime.size;
				metaSeq.sIndex = index;
				eventValue = eventValue.insert(index, id);
			};
		};
		metaSeq.loopSize = eventValue.size;
		eventCollection = kludge.compile.value; // why do i have to do this??
	}
	loadGroup { |name|
		var fh;
		if(name != nil){
			fh = File.new(root ++ sep ++ presetListSubfolder ++ sep ++ name, "r");
		}{
			"need to provide a filename".postln;
		};
		if(fh.isOpen){
			eventCollection = fh.readAllString.interpret;
			eventCollection.do{ |o,i| o.postln};
			eventCollection.postln;
		}{
			"invalid filename".postln;
		};
		fh.close;
	}
	saveGroup { |name|
		var fh, fullName;
		if(name.notNil){
			fullName = root ++ sep ++ presetListSubfolder ++ sep ++ name;
		}{
			fullName = root ++ sep ++ presetListSubfolder ++ sep ++ Date.localtime.stamp;
		};
		fh = File.new(fullName, "w");
		if(fh.isOpen){
			fh.write(eventCollection.asInfString);
			fh.close;
		}{
			postln("please create this directory manually to save loop set:"++root);
		}; 
		^fullName;
	}
	addToGroup {
		eventCollection = eventCollection.add([nextTime, eventValue]);
	}
	clear {
		if(gate){ this.freeAllVoices; };
		clock.stop;
		firstEvent = true;
		nextTime = [totalTime];
		eventValue = [nil];
	}
	load { |sel|
		var lastChoice=0;
		if(gate){ this.freeAllVoices; };
		lastChoice = currSeq;
		if(sel != nil){
			currSeq = sel.min(eventCollection.lastIndex);
			nextTime = eventCollection[currSeq][0];
			eventValue = eventCollection[currSeq][1];
		};
		metaSeq.loopSize = eventValue.size;
		metaSeq.record(currSeq - lastChoice);
	}
	numBeats_ { |beats|
		nextTime = nextTime * (beats / totalTime);
		totalTime = beats;
	}
	numBeats {
		^totalTime;
	}
	beat_ { |bt|
		index = bt % nextTime.size;
	}
	beat {
		^index;
	}
	incrementSequence {
		seqMenu.value = (seqMenu.value + 1) % seqMenu.items.size; 
		this.load(seqMenu.value);
	}
	decrementSequence {
			seqMenu.value = (seqMenu.value - 1) % seqMenu.items.size; 
			this.load(seqMenu.value);
	}
	setRecord { |val|
		if(val == 1){
			playButton.value = 1;
			this.startRecording;
		}{
			this.stopRecording;
		};
	}
	setPlay { |val|
		if(val == 1){
			this.start;
		}{
			recordButton.value = 0;
			this.stop;
		};
	}
	addSeqToGroup {
		if(seqMenu.enabled.not){
			seqMenu.enabled = true;
			incseqButtonUp.enabled = true;
			incseqButtonDown.enabled = true;
		};
		this.addToGroup;
		seqMenu.items = seqMenu.items.add(seqMenu.items.size.asString);
	}
	setCurrentGroupPreset { |item|
		this.loadGroup(item);
		if(seqMenu.enabled.not){
			seqMenu.enabled = true;
			incseqButtonUp.enabled = true;
			incseqButtonDown.enabled = true;	
		};
		seqMenu.items = Array.fill(this.eventCollection.size, { |i|
			i.asString; // for mac compatibility?
		});
	}
	savePreset {
		var pathName, fileName;			
		pathName = this.saveGroup;
		fileName = pathName.split($/).last;
		presetListMenu.items.includes(fileName).not.if{
			presetListMenu.items = presetListMenu.items.add(fileName);
		};
	}

	freeAllVoices {
		Server.default.sendMsg('n_set', synthGroup, 'gate', 0);
	}

	makeGUI { |id,parent|
		var idDisplay, idText, transport, clearButton, groups,
		addSeqGroupButton, presets, 
		presetSaveButton;
		// ID row
		idDisplay = GUI.hLayoutView.new(parent, Rect.new(0, 0, parent.view.bounds.width, 20))
					 .background_(Color.white);
		idText = GUI.staticText.new(idDisplay, idDisplay.bounds)
			.string_(id);
		// transport row
		transport = GUI.hLayoutView.new(parent, Rect.new(0, 0, parent.view.bounds.width, 30))
					 .background_(Color.new255(200, 80, 100, 180));
		recordButton = GUI.button.new(transport, Rect.new(0, 0, (transport.bounds.width - 30)/ 3, 0))
			.states_([
				["record", Color.red, Color.new255(30, 0, 0, 180)],
				["stop record", Color.black, Color.red]
			])
			.action_({ |obj| this.setRecord(obj.value); });
		playButton = GUI.button.new(transport, Rect.new(0, 0, (transport.bounds.width - 30)/ 3, 0))
			.states_([
				["play", Color.yellow, Color.new255(30, 30, 0, 180)],
				["stop", Color.black, Color.yellow]
			])
			.action_({ |obj| this.setPlay(obj.value); });
		clearButton = GUI.button.new(transport, Rect.new(0, 0, (transport.bounds.width - 30)/ 3, 0))
			.states_([["clear", Color.new255(150,150,150), Color.new255(0, 0, 0, 180)]])
			.action_({ |obj| this.clear; });
		// live group management 
		groups = GUI.hLayoutView.new(parent, Rect.new(0, 0, parent.view.bounds.width, 30))
					 .background_(Color.new255(200, 80, 200, 180));
		seqMenu = GUI.popUpMenu.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 2, 0))
			.background_(Color.new255(0, 30, 30, 180))
			.enabled_(false)
			.action_({ |obj|	this.load(obj.value); });
		Platform.case('linux', {
			seqMenu.allowsReselection_(true);
		});
		incseqButtonUp = GUI.button.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 8, 0))
			.enabled_(false)
			.states_([["^", Color.green, Color.black]])
			.action_({ this.incrementSequence; });
		incseqButtonDown = GUI.button.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 8, 0))
			.enabled_(false)
			.states_([["v", Color.green, Color.black]])
			.action_({ this.decrementSequence; });
		addSeqGroupButton = GUI.button.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 4, 0))
			.states_([["add", Color.red, Color.black]])
			.action_({ |obj| this.addSeqToGroup; });
		// load preset
		presets =  GUI.hLayoutView.new(parent, Rect.new(0, 0, parent.view.bounds.width, 30))
			.background_(Color.new255(70, 100, 100, 180));
		presetListMenu = GUI.popUpMenu.new(presets, Rect.new(0, 0, (presets.bounds.width - 50) / 1.2, 0))
			.background_(Color.new255(0, 30, 30, 180))
			.action_({ |obj| this.setCurrentGroupPreset(obj.item); });
		Platform.case('osx', {
			presetListMenu.allowsReselection_(true);
		});
		this.populatePresetListMenu;
		presetSaveButton = GUI.button.new(presets, Rect.new(0, 0, (presets.bounds.width - 50) / 4, 0));
		presetSaveButton.states = [["save", Color.green, Color.black]];
		presetSaveButton.action = { |obj| this.savePreset; };

		parent.bounds = Rect.new(
			parent.view.bounds.left, 
			parent.view.bounds.top, 
			parent.view.bounds.width,
			parent.view.bounds.height + 140
		);
	}
	populatePresetListMenu {
		pathMatch(root ++ sep ++ presetListSubfolder ++ sep ++ "*").do{ |obj|
			presetListMenu.items = presetListMenu.items.add(
				obj.split(Platform.pathSeparator).last
			);
		};
	}
}

EventLooper {
	var channelIndex=0, win, <channels, largestCollection=0, <setSeqMenu;
	*new {
		^super.new.init_eventLooper;
	}

	init_eventLooper {
		channels = Dictionary.new;
	}

	addChannel { |type, name, group|
		if(win.isNil){
			this.initGUI;
		};

		switch( type,
			0, { // monophonic
				channels = channels.add(name -> EventLooperChannel.new(name, win));
			},
			1, { // polyphonic
				channels = channels.add(name -> EventLooperChannel.new(name, win, group));
			}
		);
		^channelIndex;
	}

	incrementAllSequences {
		channels.values.do{ |obj,ind| obj.incrementSequence; }
	}

	decrementAllSequences {
		channels.values.do{ |obj,ind| obj.decrementSequence; }
	}

	setAllSequences { |seq|
		channels.values.do{ |obj,ind| obj.load(seq) }; // not working
	}

	updateLargestCollection {
		channels.values.do{ |obj,ind|
			var size=0;
			obj.seqMenu.notNil.if{ size = obj.seqMenu.items.size };
			if(size > largestCollection){
				largestCollection = size;
			};
		};
		setSeqMenu.items = Array.series(largestCollection, 0, 1);
		^largestCollection;
	}

	setAllMetaPlay { |val, button|
		channels.values.do{ |obj,ind| obj.metaSeq.setPlay(val, obj.metaSeq.pauseButton); };
		button.enabled = (val == 1);
	}

	setAllMetaPause { |val|
		channels.values.do{ |obj,ind| obj.metaSeq.setPause(val) };
	}

	setAllMetaRecord { |val|
		channels.values.do{ |obj,ind| obj.metaSeq.setRecord(val); };
	}

	setAllRecord { |val|
		channels.values.do{ |obj,ind| obj.setRecord(val); };
	}

	setAllPlay { |val|
		channels.values.do{ |obj,ind| obj.setPlay(val); };
	}

	setAllClear {
		channels.values.do{ |obj,ind| obj.clear; }
	}

	setAllTempi { |val|
		channels.values.do{ |obj,ind| 
			if(obj.clock.notNil){ obj.clock.tempo = val; }; 
		}
	}

	initGUI {
		var masterView, masterRecordButton, masterPlayButton, masterClearButton, 
			nextSeqButton, prevSeqButton, pauseButton, masterTempoSlider;
		win = GUI.window.new("Event Loopers", Rect.new(0, 300, 300, 265)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		masterView = GUI.compositeView.new(win, Rect.new(0, 0, 290, 120))
			.background_(Color.black.alpha_(0.9))
			.decorator_(FlowLayout(Rect.new(0, 0, 290, 120)));
		masterTempoSlider = EZSlider.new(
			masterView, 
			270 @ 20, 
			"tempo", 
			[0.01, 5].asSpec,
			{ |obj| this.setAllTempi(obj.value); },
			1,
			false,
			35,
			35);
		nextSeqButton = GUI.button.new(masterView, Rect.new(0, 0, 90, 20))
			.states_([["^", Color.green, Color.black]])
			.mouseDownAction_({ |obj| this.updateLargestCollection; })
			.mouseUpAction_({ |obj| this.incrementAllSequences });
		prevSeqButton = GUI.button.new(masterView, Rect.new(0, 0, 90, 20))
			.states_([["v", Color.green, Color.black]])
			.mouseDownAction_({ |obj| this.updateLargestCollection; })
			.mouseUpAction_({ |obj| this.decrementAllSequences });
		setSeqMenu = GUI.popUpMenu.new(masterView, Rect.new(0, 0, 90, 20))
			.items_(Array.series(largestCollection, 0, 1))
			.mouseDownAction_({ |obj| this.updateLargestCollection; })
			.mouseUpAction_({ |obj| this.setAllSequences(obj.value); });
		masterRecordButton = GUI.button.new(masterView, Rect.new(0, 0, 90, 20))
			.states_([
				["record", Color.red, Color.new255(30, 0, 0, 180)],
				["stop record", Color.black, Color.red]])
			.action_({ |obj| this.setAllRecord(obj.value); });
		masterPlayButton = GUI.button.new(masterView, Rect.new(0, 0, 90, 20))
			.states_([
				["play", Color.yellow, Color.new255(30, 30, 0, 180)],
				["stop", Color.black, Color.yellow]])
			.action_({ |obj| this.setAllPlay(obj.value); });
		masterClearButton = GUI.button.new(masterView, Rect.new(0, 0, 90, 20))
			.states_([["clear", Color.new255(150,150,150), Color.new255(0, 0, 0, 180)]])
			.action_({ |obj| this.setAllClear; });
		GUI.button.new(masterView, Rect.new(0, 0, 40, 20))
			.states_([[">", Color.black, Color.green],["[]", Color.black, Color.red]])
			.action_({ |obj| this.setAllMetaPlay(obj.value, pauseButton); });
		pauseButton = GUI.button.new(masterView, Rect.new(0, 0, 40, 20))
			.states_([["||", Color.yellow, Color.clear],["||", Color.black, Color.yellow]])
			.action_({ |obj| this.setAllMetaPause(obj.value); });
		GUI.button.new(masterView, Rect.new(0, 0, 65, 20))
			.states_([["record", Color.red, Color.black],["recording", Color.black, Color.red]])
			.action_({ |obj| this.setAllMetaRecord(obj.value); });
		GUI.staticText.new(masterView, Rect.new(0, 0, 100, 20))
			.stringColor_(Color.white)
			.string_("Master MetaSeq");
	}

	makeGUI { |id,parent|
		channels[id].makeGUI(id,parent);
	}

}

MetaSequence {
	var <>seq, recordCounter=0, counter=0, index=0, superIndex=0, isPlaying=false, isRecording=false, seqSize=1,
		<pauseButton;

	*new { |inSize=1, parent|
		^super.new.ms_init(inSize, parent);
	}
	ms_init { |inSize=1, parent|
		seqSize = inSize;
		seq = [[1, 0]]; // wait, delta pairs
		this.makeGUI(parent);
	}
	next {
		var ret=(-1);
		if(isRecording){ recordCounter = recordCounter + 1; };
		if(isPlaying){
			counter = counter + 1;
			if( counter == seq[index][0] ){
				ret = seq[index][1];
				index = (index + 1) % seq.size;
				counter = 0;
				postln("about to return ret: " ++ ret);
			};
		};
		^ret;
	}

	play {
		isPlaying = true;
	}

	stop {
		counter = 0;
		isPlaying = false;
	}

	pause {
		isPlaying = false;
	}

	record { |sel|
		if(isRecording){ seq = seq.add([recordCounter, sel]); };
	}

	loopSize_ { |inSize|
		seqSize = inSize;
	}

	sIndex_ { |ind|
		superIndex = ind;
	}

	setPlay { |val, button|
		if(val == 1){ 
			this.play;
			button.enabled = true;
		}{
			this.stop;
			button.enabled = false;
		};
	}

	setPause { |val|
		if(val == 1){ this.play; }{	this.pause; };
	}

	setSeq { |set|
		seq = set;
		seq.postln;
	}

	setRecord { |choice|
		isRecording = (choice == 1);
		if(isRecording){recordCounter = 0};
	}

	makeEditWindow {
		var editWin, editValue;
		editWin = GUI.window.new("edit MetaSequence", Rect.new(500.rand, 500.rand, 300, 200))
			.front;
		editWin.view
			.background_(Color.black)
			.decorator_(FlowLayout(editWin.view.bounds));
		editValue = GUI.textField.new(editWin, Rect.new(0, 0, 290, 190))
			.action_({ |obj| this.setSeq(obj.value.interpret)})
			.value_(seq.asInfString);
	}

	makeGUI { |parent|
		var controlRow;
		controlRow = GUI.hLayoutView.new(parent, Rect.new(0, 0, parent.view.bounds.width, 25))
			.background_(Color.new255(20, 10, 2));
		GUI.button.new(controlRow, Rect.new(0, 0, 40, 0))
			.states_([[">", Color.black, Color.green],["[]", Color.black, Color.red]])
			.action_({ |obj| this.setPlay(obj.value, pauseButton); });
		pauseButton = GUI.button.new(controlRow, Rect.new(0, 0, 40, 0))
			.states_([["||", Color.yellow, Color.clear],["||", Color.black, Color.yellow]])
			.action_({ |obj| this.setPause(obj.value); });
		GUI.button.new(controlRow, Rect.new(0, 0, 65, 0))
			.states_([["record", Color.red, Color.black],["recording", Color.black, Color.red]])
			.action_({ |obj| this.setRecord(obj.value); });
		GUI.button.new(controlRow, Rect.new(0, 0, 40, 0))
			.states_([["edit", Color.white, Color.black]])
			.action_({ |obj| this.makeEditWindow(obj.value); });
		GUI.staticText.new(controlRow, Rect.new(0, 0, 60, 0))
			.stringColor_(Color.white)
			.string_("MetaSeq");
	}
}

   
