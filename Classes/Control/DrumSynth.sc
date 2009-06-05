
DSBase {
	var <drumID, <rezID, drumName, rezName, groupID=999, server, 
		<>drumParams, <>rezParams, knobColors, view, <>drumControls, <>rezControls;
	*new {
		^super.new.init_dsbase;
	}
	init_dsbase {
		server = Server.default;
		knobColors = [Color.white.alpha_(0.5), Color.yellow, Color.black, Color.yellow];
	}
	hit { |lev|
		this.setDrumParam('lev', lev);
		drumID = server.nextNodeID;
		server.listSendMsg(['s_new', drumName, drumID, 0, groupID] ++ drumParams.getPairs);
	}
	startRez {
		if(rezID.isNil){ 
			rezID = server.nextNodeID; 
			server.listSendMsg(['s_new', rezName, rezID, 0, groupID] ++ rezParams.getPairs);
		}{
			postln("Node " ++ rezID ++ " not available. Synth not created.");
		};
	}
	stopRez {
		server.sendMsg('n_free', rezID);
		rezID = nil;
	}
	setRezParam { |key,val|
		rezParams[key] = val;
		server.sendMsg('n_set', rezID, key, rezParams[key]);
	}
	setDrumParam { |key,val|
		drumParams[key] = val;
	}
	refreshValues {
		rezParams.keysValuesDo{ |key,val,ind|
			if(rezControls[key].notNil){
				rezControls[key].value = val;
				server.sendMsg('n_set', rezID, key, val);
			};
		};
		drumParams.keysValuesDo{ |key,val,ind|
			if(drumControls[key].notNil){
				drumControls[key].value = val;
			};
		};
	}
}
DSKlunk : DSBase {
	*new { |parent,dParams,rParams|
		^super.new.init_dsosc(parent,dParams,rParams);
	}
	init_dsosc { |parent,dParams,rParams|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_gray';
		rezName = 'r_klank';
		drumParams = dParams;
		rezParams = rParams;
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rezControls = Dictionary[
			'f1' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "f1")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['f1'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('f1', obj.value); }),
			'f2' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "f2")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['f2'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('f2', obj.value); }),
			'f3' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "f3")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['f3'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('f3', obj.value); }),
			'r1' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "r1")
				.spec_([0.001, 2].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['r1'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('r1', obj.value); }),
			'r2' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "r2")
				.spec_([0.001, 2].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['r2'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('r2', obj.value); }),
			'r3' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "r3")
				.spec_([0.001, 2].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['r3'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('r3', obj.value); }),
			'a1' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "a1")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['a1'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('a1', obj.value); }),
			'a2' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "a2")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['a2'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('a2', obj.value); }),
			'a3' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "a3")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['a3'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('a3', obj.value); }),
			'lev' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rLev")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['lev'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('lev', obj.value); })
		];
		drumControls = Dictionary[
			'att' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
				.spec_([0.0001, 0.3, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['att'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('att', obj.value); }),
			'rel' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
				.spec_([0.0001, 1, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['rel'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('rel', obj.value); }),
			'lev' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xLev")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['lev'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('lev', obj.value); }),
			'curve' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
				.spec_([-10, 10].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['curve'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('curve', obj.value); })
		];
	}
}

DSOsc : DSBase {
	var rFreqKnob, rResKnob, rGainKnob,
		xAttKnob, xRelKnob, xCurveKnob, xFreqKnob, 
		xModPhaseKnob, xFreqKnob, xModFreqKnob, xModAmtKnob, xDriveKnob;
	*new { |parent,dParams,rParams|
		^super.new.init_dsosc(parent,dParams,rParams);
	}
	init_dsosc { |parent,dParams,rParams|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_osc';
		rezName = 'r_lpf';
		drumParams = dParams;
		rezParams = rParams;
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rezControls = Dictionary[
			'freq' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "cut")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['freq'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('freq', obj.value); }),
			'res' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "res")
				.spec_([1, 100].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['res'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('res', obj.value); }),
			'gain' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rGain")
				.spec_([0.001, 2, 2].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['gain'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('gain', obj.value); })
		];
		drumControls = Dictionary[
			'att' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
				.spec_([0.0001, 0.5, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['att'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('att', obj.value); }),
			'rel' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
				.spec_([0.0001, 2, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['rel'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('rel', obj.value); }),
			'curve' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
				.spec_([-10, 10].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['curve'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('curve', obj.value); }),
			'freq' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "freq")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['freq'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('freq', obj.value); }),
			'modPhase' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "mPhase")
				.spec_([0, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['modPhase'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('modPhase', obj.value); }),
			'modFreq' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "mFreq")
				.spec_([0, 20, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['modFreq'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('modFreq', obj.value); }),
			'modAmt' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "mAmt")
				.spec_([0, 20, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['modAmt'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('modAmt', obj.value); }),
			'drive' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "drive")
				.spec_([0.0001, 3, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['drive'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('drive', obj.value); })
		];
	}
}

DSHiHat : DSBase {
	var rFreqKnob, rResKnob, rGainKnob, xAttKnob, xRelKnob, xLevKnob, xCurveKnob;
	*new { |parent,drumParams,rezParams|
		^super.new.init_dsosc(parent,drumParams,rezParams);
	}
	init_dsosc { |parent,dParams,rParams|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_crackle';
		rezName = 'r_hpf';
		drumParams = dParams;
		rezParams = rParams;
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rezControls = Dictionary[
			'freq' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "cut")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['freq'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('freq', obj.value); }),
			'res' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "res")
				.spec_([1, 150].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['res'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('res', obj.value); }),
			'gain' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rGain")
				.spec_([0.001, 2, 2].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['gain'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('gain', obj.value); })
		];
		drumControls = Dictionary[
			'att' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
				.spec_([0.0001, 0.3, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['att'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('att', obj.value); }),
			'rel' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
				.spec_([0.001, 1, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['rel'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('rel', obj.value); }),
			'lev' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "lev")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				//.value_(drumParams['lev'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('lev', obj.value); }),
			'curve' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
				.spec_([-10, 10].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['curve'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('curve', obj.value); })
		];
	}
}
 // x_whiteSnare r_lpf
DSSnare  : DSBase {
	var rFreqKnob, rResKnob, rGainKnob, xFreqKnob, xRezKnob, xGainKnob, xAttKnob, xRelKnob, xLevKnob, xCurveKnob;
	*new { |parent,drumParams,rezParams|
		^super.new.init_dsosc(parent,drumParams,rezParams);
	}
	init_dsosc { |parent,dParams,rParams|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_whiteSnare';
		rezName = 'r_lpf';
		drumParams = dParams;
		rezParams = rParams;
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rezControls = Dictionary[
			'freq' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "cut")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['freq'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('freq', obj.value); }),
			'res' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "res")
				.spec_([1, 150].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['res'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('res', obj.value); }),
			'gain' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rGain")
				.spec_([0.001, 2, 2].asSpec)
				.knobColor_(knobColors)
				.value_(rezParams['gain'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setRezParam('gain', obj.value); })		];
		drumControls = Dictionary[
			'freq' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xFreq")
				.spec_('freq'.asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['freq'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('freq', obj.value); }),
			'rez' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xRez")
				.spec_([1, 5].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['rez'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('rez', obj.value); }),
			'gain' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xGain")
				.spec_([0.001, 2, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['gain'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('gain', obj.value); }),
			'att' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
				.spec_([0.0001, 0.3, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['att'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('att', obj.value); }),
			'rel' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
				.spec_([0.001, 1, 2].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['rel'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('rel', obj.value); }),
			'lev' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "lev")
				.spec_('amp'.asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['lev'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('lev', obj.value); }),
			'curve' -> EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
				.spec_([-10, 10].asSpec)
				.knobColor_(knobColors)
				.value_(drumParams['curve'])
				.stringColor_(Color.yellow)
				.knobAction_({ |obj| this.setDrumParam('curve', obj.value); })
		];
	}
}

DrumSynth {
	classvar <lastNote;
	var win, <drums, <>noteOns, <>drumXGroup=999, drumSynthGroup=400, outBus=0,
		drumCCCommands, drumParams, rezParams, <recorderID="drumSynth", server, saveRoot, sep,
		presetNameField, presetMenu;
	*new { |name|
		^super.new.init_drumsynth(name);
	}
	init_drumsynth { |name|
		server = Server.default;
		server.sendMsg('g_new', drumSynthGroup, 0, 1);
		server.sendMsg('g_new', drumXGroup, 0, drumSynthGroup);
		if(name.notNil){ recorderID = name; };
		noteOns = [50, 45, 51, 49, 36, 38, 46, 42];
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ "DrumSynth";
		this.addMixerChannel;
		this.initGUI;
		drums = [
			DSKlunk.new(
				win, 
				Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 10, 'lev' -> 0.1], 
				Dictionary['lev' -> 1, 'f1' -> 100, 'f2' -> 80, 'f3'-> 250,
					  'r1' -> 0.1, 'r2' -> 0.5, 'r3' -> 0.8,
					  'a1' -> 0.01, 'a2' -> 0.1, 'a3' -> 0.11, 'inBus' -> 10, 'outBus' -> outBus]), 
			DSKlunk.new(
				win, 
				Dictionary['lev' -> 1, 'curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 11], 
				Dictionary['lev' -> 1, 'f1' -> 100, 'f2' -> 80, 'f3'-> 250,
					'r1' -> 0.1, 'r2' -> 0.5, 'r3' -> 0.8,
					'a1' -> 0.1, 'a2' -> 0.11, 'a3' -> 0.08, 'inBus' -> 11, 'outBus' -> outBus]), 
			DSHiHat.new(
				win, 
				Dictionary['lev' -> 1, 'curve' -> -10, 'att' -> 0.0001, 'rel' -> 0.6, 'outBus' -> 12, 
					  'crackle' -> 1.5, 'gain' -> 2], 
				Dictionary['freq' -> 600, 'res' -> 10, 'inBus' -> 12, 'gain' -> 1, 'outBus' -> outBus]), 
			DSHiHat.new(
				win, 
				Dictionary['lev' -> 1, 'curve' -> -5, 'att' -> 0.0001, 'rel' -> 0.9, 'outBus' -> 13, 
					  'crackle' -> 1.5, 'gain' -> 1.3], 
				Dictionary['lev' -> 1, 'freq' -> 600, 'res' -> 10, 'inBus' -> 13, 'gain' -> 1, 'outBus' -> outBus]),
			DSKlunk.new(
				win, 
				Dictionary['lev' -> 1, 'curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 14], 
				Dictionary['lev' -> 1, 'f1' -> 120, 'f2' -> 75, 'f3'-> 55,
					  'r1' -> 0.2, 'r2' -> 0.1, 'r3' -> 0.1,
					  'a1' -> 0.2, 'a2' -> 0.05, 'a3' -> 0.1, 'inBus' -> 14, 'outBus' -> outBus]), 
			DSOsc.new(
				win, 
				Dictionary['curve' -> -5, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 15,
					'freq' -> 80, 'modPhase' -> 0, 'modFreq' -> -1, 'modAmt' -> 0, 'drive' -> 10], 
				Dictionary['lev' -> 1, 'freq' -> 1600, 'res' -> 1, 'gain' -> 2, 'inBus' -> 15, 'outBus' -> outBus]),   
			DSOsc.new(
				win, 
				Dictionary['curve' -> -5, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 16,
					'freq' -> 80, 'modPhase' -> 0, 'modFreq' -> -1, 'modAmt' -> 0, 'drive' -> 10], 
				Dictionary['lev' -> 1, 'freq' -> 1600, 'res' -> 1, 'gain' -> 2, 'inBus' -> 16, 'outBus' -> outBus]),   
			DSSnare.new(
				win, 
				Dictionary['lev' -> 1, 'curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 17,
					   'gain' -> 3, 'freq' -> 1000, 'rez' -> 2], 
				Dictionary['lev' -> 1, 'freq' -> 2600, 'res' -> 1, 'gain' -> 2, 'inBus' -> 17, 'outBus' -> outBus])
		];
		this.initLooper;

	}
	addMixerChannel {
		~mixer.addMonoChannel("DrumSynth", ~mixer.mixGroup);
		outBus = ~mixer.channels["DrumSynth"].inBus;
	}
	noteOn { |src,chan,num,vel|
		var voice;
		voice = noteOns.indexOf(num);
		if(voice.notNil){
			drums[voice].hit(vel / 127);
		};
	}
	cc { |src,chan,num,val|
		postln("does nothing");
	}
	looper {
		^~eventLooper.channels[recorderID];
	}
	getParams {
		var ret, drumDict, rezDict;
		drumDict = Dictionary.new;
		rezDict = Dictionary.new;
		drums.do{ |obj, ind|
			ret = ret.add([
				this.formatParams(obj.drumParams), 
				this.formatParams(obj.rezParams)
			]);
		};
		^ret;
	}
	formatParams { |params|
		var ret;
		ret = Dictionary.new;
		params.keysValuesDo{ |key,val,ind|
			ret = ret.add('\'' ++ key ++ '\'' -> val);
		}
		^ret;
	}
	savePreset { |name|
		var params, fileName, filePath, fh, pipe;

		params = this.getParams;
		if(name == "<>"){
			fileName = Date.localtime.stamp;
		}{
			fileName = name;
		};
		
		filePath = saveRoot ++ sep ++ fileName;
		fh = File.new(filePath, "w");
		if(fh.isOpen){
			fh.write(params.asInfString);
			fh.close;
		}{
			postln("creating save directory " ++ saveRoot);
			pipe = Pipe.new("mkdir -p \"" ++ saveRoot ++ "\"", "w");
			pipe.close;
			fh = File.new(filePath, "w");
			if(fh.isOpen){
				fh.write(params.asInfString);
				fh.close
			}{
				postln("preset save operation failed");
			};
			
		};
		presetMenu.items_(this.getPresetList);
		presetNameField.string_("<>");
	}
	loadPreset { |presetName|
		var preset;
		preset = (saveRoot ++ sep ++ presetName).load;
		preset.do{ |obj,ind|
			drums[ind].drumParams = obj[0];
			drums[ind].rezParams = obj[1];
			drums[ind].refreshValues;
		};
	}
	initLooper {
		~eventLooper.addChannel(0, recorderID);
		~eventLooper.channels[recorderID].action = { |values, index|
			this.noteOn(nil, nil, values[0], values[1]);
		};
	}
	getPresetList {
		^(saveRoot ++ sep ++ "*").pathMatch.collect{ |obj,ind| obj.split($/).last; };
	}
	initGUI {
		var presetRow, saveButton;
		win = GUI.window.new("DrumSynth", Rect.new(500.rand, 500.rand, 550, 625)).front;
		win.view.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
		presetRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.white.alpha_(0.8));
		saveButton = GUI.button.new(presetRow, Rect.new(0, 0, 75, 0))
			.states_([["save", Color.black, Color.green]])
			.action_({ |obj| this.savePreset(presetNameField.string); });
		presetNameField = GUI.textField.new(presetRow, Rect.new(0, 0, 75, 0))
			.action_({ |obj| this.savePreset(obj.string); })
			.string_("<>");
		presetMenu = GUI.popUpMenu.new(presetRow, Rect.new(0, 0, 230, 0))
			.items_(this.getPresetList)
			.action_({ |obj| this.loadPreset(obj.item) });

	}
}

