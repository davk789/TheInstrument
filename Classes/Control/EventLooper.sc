EventLooperChannel {
	classvar <>root;
	var <>clock, <totalTime, index, nextTime, <eventValue, lastEvent, iterator, >action, 
		waitTime=0, firstEvent=true, <>isRecording=false, <eventCollection, <metaSeq, 
		currSeq=0, cTempo=1;
	*new { |win, name|
		^super.new.init_eventLooperChannel(win, name);
	}
	init_eventLooperChannel { |win, name|
		var sep;
		sep = Platform.pathSeparator;
		root = Platform.userAppSupportDir ++ sep ++ "EventLooperGroups" ++ sep ++ "";
		metaSeq = MetaSequence.new(eventValue.size);
		totalTime = 8;
		index = 0;
		nextTime = [totalTime];
		eventValue = [nil];
		eventCollection = [];
		/*action = { |id,beat|
		    postln("waiting " ++ waitTime ++ " beats");
		};*/
		iterator = this.itr;
	}
	itr {
		^{
			var nextSeq;
			lastEvent = clock.beats;
			index = (index + 1) % nextTime.size;
			metaSeq.sIndex = index;
			waitTime = nextTime[index];
			if(eventValue[index] != nil){
				action.value(eventValue[index],index);
				postln("waiting " ++ waitTime ++ " beats");
			};
			// metaSequence iterator
			nextSeq = metaSeq.next;
			if(nextSeq != -1){
				this.load((nextSeq + currSeq) % eventCollection.size);
				postln("load this sequence: " ++ "( " ++ nextSeq ++ " + " ++ currSeq ++ " ) % "++eventCollection.size);
			};
			waitTime;
		};
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
		firstEvent = false;
		clock = TempoClock.new(cTempo);
		iterator.value;
		clock.schedAbs(waitTime + clock.beats, iterator);
	}
	stop {
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
			fh = File.new(root ++ name, "r");
		}{
			"need to provide a filename".postln;
		};
		if(fh.isOpen){
			eventCollection = fh.readAllString.compile.value;
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
			fullName = root ++ name;
		}{
			fullName = root ++ Date.localtime.stamp;			
		};
		fh = File.new(fullName, "w");
		if(fh.isOpen){
			fh.write(eventCollection.asInfString);
			fh.close;
		}{
			postln("please create this directory manually to save loop set:\n"++root);
		}; // this should automatically create the directory "root", but it doesn't right now
		^fullName;
	}
	addToGroup {
		eventCollection = eventCollection.add([nextTime, eventValue]);
	}
	clear {
		clock.stop;
		firstEvent = true;
		nextTime = [totalTime];
		eventValue = [nil];
	}
	load { |sel|
		if(sel != nil){
			currSeq = sel;
			nextTime = eventCollection[sel][0];
			eventValue = eventCollection[sel][1];
		};
		metaSeq.loopSize = eventValue.size;
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

}
SynthEventLooperChannel : EventLooperChannel {
	var <>synthGroup;	
	*init { |group|
		^super.new.init_synthEventLooper(group);
	}
	init_synthEventLooper { |group|
		if(group.notNil){
			synthGroup = group;
		};
	}
	load { |sel|
		Server.default.sendMsg('n_set', synthGroup, 'gate', 0);
		if(sel != nil){
			currSeq = sel;
			nextTime = eventCollection[sel][0];
			eventValue = eventCollection[sel][1];
		};
		metaSeq.loopSize = eventValue.size;
	}
	clear {
		Server.default.sendMsg('n_set', synthGroup, 'gate', 0);
		clock.stop;
		firstEvent = true;
		nextTime = [totalTime];
		eventValue = [nil];		
	}
	stop {
		Server.default.sendMsg('n_set', synthGroup, 'gate', 0);
		isRecording = false;
		clock.stop;
	}
}

EventLooper {
	var channelIndex=0, win, <channels, <>seqMenu, <>playButton, <>recordButton;
	*new { |...args|
		^super.new.init_eventLooper(args);
	}
	init_eventLooper { |args|
		channels = Dictionary.new;
		if(args.size > 0){
			args.do{ |obj,ind|
				this.addChannel(obj, ind);
			};
		};
	}
	addChannel { |type, name|
		if(win.isNil){
			this.initGUI;
		};
		switch( type,
			0, {
				channels = channels.add(name -> EventLooperChannel.new);
			},
			1, {
				channels = channels.add(name -> SynthEventLooperChannel.new);
			}
		);
		this.makeGUI(name);
		^channelIndex;
	}
	initGUI {
		win = GUI.window.new("Event Loopers", Rect.new(0, 300, 300, 0));
		win.view.decorator = FlowLayout(win.view.bounds);
		win.front;
	}
// GUI methods
	makeGUI { |id|
		var idDisplay, idText, transport, recordButton, clearButton, groups,
		incseqButtonUp, incseqButtonDown, addSeqGroupButton, presets, presetListMenu, 
		presetSaveButton;
		// ID row
		idDisplay = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
					 .background_(Color.white);//black);
		idText = GUI.staticText.new(idDisplay, idDisplay.bounds)
			.string_(id)
			//.stringColor_(Color.white)
			/*.font_(Font.new("Arial", 9))*/;
		// transport row
		transport = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 30))
					 .background_(Color.new255(200, 80, 100, 180));
		
		recordButton = GUI.button.new(transport, Rect.new(0, 0, (transport.bounds.width - 30)/ 3, 0))
					 /*.font_(Font.new("Arial", 9))*/;
		recordButton.states = [
			["record", Color.red, Color.new255(30, 0, 0, 180)],
			["stop record", Color.black, Color.red]
		];
		recordButton.action = { |obj|
			if(obj.value == 1){
				playButton.value = 1;
				channels[id].startRecording;
			}{
				channels[id].stopRecording;
			};
		};
		
		playButton = GUI.button.new(transport, Rect.new(0, 0, (transport.bounds.width - 30)/ 3, 0))
					 /*.font_(Font.new("Arial", 9))*/;
		playButton.states = [
			["play", Color.yellow, Color.new255(30, 30, 0, 180)],
			["stop", Color.black, Color.yellow]
		];
		playButton.action = { |obj|
			if(obj.value == 1){
				channels[id].start;
			}{
				recordButton.value = 0;
				channels[id].stop;
			};
		};
		
		clearButton = GUI.button.new(transport, Rect.new(0, 0, (transport.bounds.width - 30)/ 3, 0))
					 /*.font_(Font.new("Arial", 9))*/;
		clearButton.states = [["clear", Color.new255(150,150,150), Color.new255(0, 0, 0, 180)]];
		clearButton.action = { |obj|
			channels[id].clear;
		};
		
		// live group management 
		
		groups = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 30))
					 .background_(Color.new255(200, 80, 200, 180));
		
		seqMenu = GUI.popUpMenu.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 2, 0))
					 /*.font_(Font.new("Arial", 9))*/
					 .background_(Color.new255(0, 30, 30, 180))
					 //.stringColor_(Color.new255(0, 255, 255))
					 .enabled_(false)
					 /*.allowsReselection_(true)*/;
		seqMenu.action = { |obj|
			channels[id].load(obj.value);
		};
		
		incseqButtonUp = GUI.button.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 8, 0))
					 /*.font_(Font.new("Arial", 9))*/
					 .enabled_(false);
		incseqButtonUp.states = [["^", Color.green, Color.black]];
		incseqButtonUp.action = { |obj|
			seqMenu.value = (seqMenu.value + 1) % seqMenu.items.size; 
			channels[id].load(seqMenu.value);
		};
		
		incseqButtonDown = GUI.button.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 8, 0))
					 /*.font_(Font.new("Arial", 9))*/
					 .enabled_(false);
		incseqButtonDown.states = [["v", Color.green, Color.black]];
		incseqButtonDown.action = { |obj|
			seqMenu.value = (seqMenu.value - 1) % seqMenu.items.size;
			channels[id].load(seqMenu.value);
		};
		
		addSeqGroupButton = GUI.button.new(groups, Rect.new(0, 0, (groups.bounds.width - 50) / 4, 0))
					 /*.font_(Font.new("Arial", 9))*/;
		addSeqGroupButton.states = [["add", Color.red, Color.black]];
		addSeqGroupButton.action = { |obj|
			if(seqMenu.enabled.not){
				seqMenu.enabled = true;
				incseqButtonUp.enabled = true;
				incseqButtonDown.enabled = true;		
			};
			channels[id].addToGroup;
			seqMenu.items = seqMenu.items.add(seqMenu.items.size.asString);
		};
		// load preset
		presets =  GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 30))
					 .background_(Color.new255(70, 100, 100, 180));
		presetListMenu = GUI.popUpMenu.new(presets, Rect.new(0, 0, (presets.bounds.width - 50) / 1.2, 0))
					 /*.font_(Font.new("Arial", 9))*/
					 .background_(Color.new255(0, 30, 30, 180));
					 //.stringColor_(Color.new255(0, 255, 255))
					 //.enabled_(false);
					 //.allowsReselection_(true);
		presetListMenu.action = { |obj|
			channels[id].loadGroup(obj.items[obj.value]);
			if(seqMenu.enabled.not){
				seqMenu.enabled = true;
				incseqButtonUp.enabled = true;
				incseqButtonDown.enabled = true;		
			};
			seqMenu.items = Array.fill(channels[id].eventCollection.size, { |i|
				i.asString; // for mac compatibility?
			});
		};
		
		pathMatch(EventLooperChannel.root ++ "*").do{ |obj|
			presetListMenu.items = presetListMenu.items.add(
				obj.split(Platform.pathSeparator).last
			);
		};

		presetSaveButton = GUI.button.new(presets, Rect.new(0, 0, (presets.bounds.width - 50) / 4, 0))
					 /*.font_(Font.new("Arial", 9))*/;
		presetSaveButton.states = [["save", Color.green, Color.black]];
		presetSaveButton.action = { |obj|
			var pathName, fileName;			
			pathName = channels[id].saveGroup;
			fileName = pathName.split($/).last;
			if(presetListMenu.items.includes(fileName).not){
				presetListMenu.items = presetListMenu.items.add(fileName);
			};
		};
		win.bounds = Rect.new(
			win.view.bounds.left, 
			win.view.bounds.top, 
			win.view.bounds.width,
			win.view.bounds.height + 110
		);
	}
}

MetaSequence {
	var <>seq, counter=0, index=0, superIndex=0, isPlaying=false, seqSize=1;
	
	*new { |inSize=1|
		^super.new.ms_init(inSize);
	}
	ms_init { |inSize=1|
		seqSize = inSize;
		seq = Dictionary['wait' -> [1], 'delta' -> [0]];
	}
	next {
		var ret=(-1);
		if(isPlaying){
			counter = counter + 1;
			if( counter == seq['wait'][index] ){
				ret = seq['delta'][index];
				index = (index + 1) % seq['wait'].size;
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
		isPlaying = false;
	}
	// ~~~~~~~~~~ data MetaSequence needs from EventLooper
	loopSize_ { |inSize|
		seqSize = inSize;
	}
	sIndex_ { |ind|
		superIndex = ind;
	}
}
/*
  ...to do later:
  add pause/resume
  fix the hiccup on starting the sequence
  add a recordable metasequence
  make the stupid thing actually work
 */

                                                                                      
   