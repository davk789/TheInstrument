/*
		drumParams = [			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 10, 'gain' -> 0.1], // x_gray
			Dictionary['curve' -> -5, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 11,
						  'freq' -> 80, 'modPhase' -> 0, 'modFreq' -> -1, 'modAmt' -> 0, 'drive' -> 10], // x_osc
			Dictionary['curve' -> -10, 'att' -> 0.0001, 'rel' -> 0.6, 'outBus' -> 12, 
					  'crackle' -> 1.5, 'gain' -> 2], // x_crack
			Dictionary['curve' -> -5, 'att' -> 0.0001, 'rel' -> 0.9, 'outBus' -> 13, 
					  'crackle' -> 1.5, 'gain' -> 1.3], // x_crack
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 14], //x_clip
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 15], //x_clip
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 16], //x_clip
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 17,
					   'gain' -> 3, 'freq' -> 1000, 'rez' -> 2] // x_whiteSnare
		];
		drumParams.do{ |obj,ind|
			drum[ind].params = obj;
		};
		rezParams = [
			Dictionary['f1' -> 100, 'f2' -> 80, 'f3'-> 250,
					  'r1' -> 0.1, 'r2' -> 0.5, 'r3' -> 0.8,
					  'a1' -> 0.2, 'a2' -> 0.3, 'a3' -> 0.4, 'inBus' -> 10], // r_klank
			Dictionary['freq' -> 1600, 'res' -> 10, 'gain' -> 2, 'inBus' -> 11], // r_lpf
			Dictionary['freq' -> 600, 'res' -> 10, 'inBus' -> 12], // r_hpf
			Dictionary['freq' -> 600, 'res' -> 10, 'inBus' -> 13], // r_hpf
			Dictionary['f1' -> 120, 'f2' -> 75, 'f3'-> 55,
					  'r1' -> 0.2, 'r2' -> 0.1, 'r3' -> 0.1,
					  'a1' -> 0.4, 'a2' -> 0.3, 'a3' -> 0.2, 'inBus' -> 14], // r_klank
			Dictionary['f1' -> 1200, 'f2' -> 900, 'f3'-> 900,
					  'r1' -> 0.5, 'r2' -> 0.1, 'r3' -> 0.2,
					  'a1' -> 0.3, 'a2' -> 0.4, 'a3' -> 0.4, 'inBus' -> 15], // r_klank
			Dictionary['f1' -> 80, 'f2' -> 95, 'f3'-> 145,
					  'r1' -> 0.5, 'r2' -> 0.4, 'r3' -> 0.6,
					  'a1' -> 0.4, 'a2' -> 0.3, 'a3' -> 0.2, 'inBus' -> 16], // r_klank
			Dictionary['freq' -> 2600, 'res' -> 1, 'gain' -> 2, 'inBus' -> 17] // r_lpf
		];
		rezParams.do{ |obj,ind|
			rez[ind].params = obj;
		};
*/

DSBase {
	var <drumID, <rezID, drumName, rezName, groupID=999, server, <>drumParams, <>rezParams, knobColors, view;
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
		server.listSendMsg(['s_new', 'x_osc', drumID, 0, groupID] ++ drumParams.getPairs);
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
}
DSKlunk : DSBase {
	var rFreqKnob, rResKnob, 
		f1Knob, f2Knob, f3Knob, 
		r1Knob, r2Knob, r3Knob, 
		a1Knob, a2Knob, a3Knob, rLevKnob, 
		xAttKnob, xRelKnob, xLevKnob, xCurveKnob;
	*new { |parent|
		^super.new.init_dsosc(parent);
	}
	init_dsosc { |parent|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_gray';
		rezName = 'r_klank';
		drumParams = Dictionary['att' -> 0.001, 'rel' -> 0.5, 'lev' -> 0.5, 'curve' -> 2, 'outBus' -> 14];
		rezParams = Dictionary[
			'lev' -> 0.7, 
			'f1' -> 80, 'f2' -> 90, 'f3' -> 145,
			'r1' -> 5, 'r2' -> 4, 'r3' -> 6,
			'a1' -> 0.7, 'a2' -> 0.8, 'a3' -> 0.4
		];
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		f1Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "f1")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['f1'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('f1', obj.value); });
		f2Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "f2")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['f2'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('f2', obj.value); });
		f3Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "f3")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['f3'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('f3', obj.value); });
		r1Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "r1")
			.spec_([1, 150].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['r1'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('r1', obj.value); });
		r2Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "r2")
			.spec_([1, 150].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['r2'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('r2', obj.value); });
		r3Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "r3")
			.spec_([1, 150].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['r3'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('r3', obj.value); });
		a1Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "a1")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['a1'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('a1', obj.value); });
		a2Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "a2")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['a2'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('a2', obj.value); });
		a3Knob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "a3")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['a3'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('a3', obj.value); });
		rLevKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rLev")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['lev'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('lev', obj.value); });
		xAttKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
			.spec_([0.0001, 0.3, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['att'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('att', obj.value); });
		xRelKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
			.spec_([0.0001, 1, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['rel'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('rel', obj.value); });		
		xLevKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xLev")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['lev'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('lev', obj.value); });
		xCurveKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
			.spec_([-3, 3].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['curve'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('curve', obj.value); });
	}
}

DSOsc : DSBase {
	var rFreqKnob, rResKnob, rGainKnob,
		xAttKnob, xRelKnob, xCurveKnob, xFreqKnob, 
		xModPhaseKnob, xModFreqKnob, xModAmtKnob, xDriveKnob;
	*new { |parent|
		^super.new.init_dsosc(parent);
	}
	init_dsosc { |parent|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_osc';
		rezName = 'r_lpf';
		drumParams = Dictionary[
			'att' -> 0.001, 'rel' -> 0.5, 'lev' -> 1, 'curve' -> 2, 
			'freq' -> 80, 'modPhase' -> 0, 'modFreq' -> -1, 'modAmt' -> 0, 
			'drive' -> 0, 'outBus' -> 15
		];
		rezParams = Dictionary[
			'freq' -> 1600, 'res' -> 10,
			'outBus' -> 0, 'inBus' -> 12, 'lev' -> 1, 'gain' -> 1
		];
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rFreqKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "cut")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['freq'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('freq', obj.value); });
		rResKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "res")
			.spec_([1, 100].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['res'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('res', obj.value); });
		rGainKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rGain")
			.spec_([0.001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['gain'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('res', obj.value); });
		xAttKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
			.spec_([0.0001, 0.5, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['att'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('att', obj.value); });
		xRelKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
			.spec_([0.0001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['rel'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('rel', obj.value); });
		xCurveKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
			.spec_([-5, 5].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['curve'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('curve', obj.value); });
		xModPhaseKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "mPhase")
			.spec_([0, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['modPhase'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('modPhase', obj.value); });
		xModFreqKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "mFreq")
			.spec_([0, 20, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['modFreq'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('modFreq', obj.value); });
		xModAmtKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "mAmt")
			.spec_([0, 20, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['modAmt'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('modAmt', obj.value); });
		xDriveKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "drive")
			.spec_([0.0001, 0.3, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['drive'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('drive', obj.value); });
		
	}
}

DSHiHat : DSBase {
	var rFreqKnob, rResKnob, rGainKnob, xAttKnob, xRelKnob, xLevKnob, xCurveKnob;
	*new { |parent|
		^super.new.init_dsosc(parent);
	}
	init_dsosc { |parent|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_crackle';
		rezName = 'r_hpf';
		drumParams = Dictionary[
			'curve' -> -10, 'att' -> 0.0001, 'rel' -> 0.6, 'outBus' -> 12, 
			'crackle' -> 1.5, 'gain' -> 2
		];
		rezParams = Dictionary[
			'freq' -> 1600, 'res' -> 10,
			'outBus' -> 0, 'inBus' -> 12, 'lev' -> 1, 'gain' -> 1
		];
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rFreqKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "cut")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['freq'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('freq', obj.value); });
		rResKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "res")
			.spec_([1, 150].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['res'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('res', obj.value); });
		rGainKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rGain")
			.spec_([0.001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['gain'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('gain', obj.value); });	
		xAttKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
			.spec_([0.0001, 0.3, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['att'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('att', obj.value); });
		xRelKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
			.spec_([0.001, 1, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['rel'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('rel', obj.value); });
		xLevKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "lev")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['lev'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('lev', obj.value); });
		xCurveKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
			.spec_([-3, 3].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['curve'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('curve', obj.value); });
	}
}
 // x_whiteSnare r_lpf
DSSnare  : DSBase {
	var rFreqKnob, rResKnob, rGainKnob, xFreqKnob, xRezKnob, xGainKnob, xAttKnob, xRelKnob, xLevKnob, xCurveKnob;
	*new { |parent|
		^super.new.init_dsosc(parent);
	}
	init_dsosc { |parent|
		postln(this.class.asString ++ " initialized");
		server = Server.default;
		drumName = 'x_whiteSnare';
		rezName = 'r_lpf';
		drumParams = Dictionary[
			'outBus' -> 10, 'freq' -> 1200, 'gain' -> 1, 'rez' -> 2, 
			'att' -> 0.011, 'rel' -> 0.5, 'lev' -> 1, 'curve' -> -10
		];
		rezParams = Dictionary[
			'freq' -> 1600, 'res' -> 10,
			'outBus' -> 0, 'inBus' -> 12, 'lev' -> 1, 'gain' -> 1
		];
		this.initGUI(parent);
		this.startRez;
	}
	initGUI { |parent|
		view = GUI.compositeView.new(parent, Rect.new(0, 0, 500, 65))
			.background_(Color.blue(0.1, alpha:0.2));
		view.decorator = FlowLayout(view.bounds);
		rFreqKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "cut")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['freq'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('freq', obj.value); });
		rResKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "res")
			.spec_([1, 150].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['res'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('res', obj.value); });
		rGainKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rGain")
			.spec_([0.001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(rezParams['gain'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRezParam('gain', obj.value); });
		xFreqKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xFreq")
			.spec_('freq'.asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['freq'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('freq', obj.value); });
		xRezKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xRez")
			.spec_([1, 150].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['rez'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('rez', obj.value); });
		xGainKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "xGain")
			.spec_([0.001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['gain'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('gain', obj.value); });
		xAttKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "att")
			.spec_([0.0001, 0.3, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['att'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('att', obj.value); });
		xRelKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "rel")
			.spec_([0.001, 1, 2].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['rel'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('rel', obj.value); });
		xLevKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "lev")
			.spec_('amp'.asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['lev'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('lev', obj.value); });
		xCurveKnob = EZJKnob.new(view, Rect.new(0, 0, 30, 60), "curve")
			.spec_([-3, 3].asSpec)
			.knobColor_(knobColors)
			.value_(drumParams['curve'])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setDrumParam('curve', obj.value); });
	}
}

DrumSynth {
	classvar <lastNote;
	var win, <drums, <>noteOns, <>drumXGroup=999, drumSynthGroup=400,
		drumCCCommands, drumParams, rezParams, <recorderID="drumSynth", server;
	*new { |name|
		^super.new.init_drumsynth(name);
	}
	init_drumsynth { |name|
		server = Server.default;
		server.sendMsg('g_new', drumSynthGroup, 0, 1);
		server.sendMsg('g_new', drumXGroup, 0, drumSynthGroup);
		if(name.notNil){ recorderID = name; };
		noteOns = [50, 45, 51, 49, 36, 38, 46, 42];
		this.initGUI;
		drums = [
			DSKlunk.new(win), DSKlunk.new(win), DSHiHat.new(win), DSHiHat.new(win),
			DSKlunk.new(win), DSOsc.new(win),   DSOsc.new(win),   DSSnare.new(win)
		];
		this.initLooper;
	}
	noteOn { |src,chan,num,vel|
		var voice;
		voice = noteOns.indexOf(num);
		voice.postln;
		if(voice.notNil){
			drums[voice].hit(vel / 127);
		};
	}
	cc { |src,chan,num,val|
		postln("does nothing");
	}
	initLooper {
		~eventLooper.addChannel(0, recorderID);
		~eventLooper.channels[recorderID].action = { |values, index|
			this.noteOn(nil, nil, values[0], values[1]);
		};
	}
	looper {
		^~eventLooper.channels[recorderID];
	}
	initGUI {
		win = GUI.window.new("DrumSynth", Rect.new(500.rand, 500.rand, 550, 600)).front;
		win.view.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
	}
}


/*



DrumSynth {
	classvar <lastNote;
	var <>drum, <>rez, drumDefs, rezDefs, <>noteOns, <>drumXGroup=999, drumSynthGroup=400,
		drumCCCommands, drumParams, rezParams, <recorderID="drumSynth", s;
	*new { |name|
		^super.new.init_drumsynth(name);
	}
	init_drumsynth { |name|
		s = Server.default;
		s.sendMsg('g_new', drumSynthGroup, 0, 1);
		s.sendMsg('g_new', drumXGroup, 0, drumSynthGroup);
		if(name.notNil){ recorderID = name; };
		drumDefs = ['x_gray',  'x_osc',  'x_crackle', 'x_crackle', // row 1 
					'x_clip',  'x_clip', 'x_clip',    'x_whiteSnare'];   // row 2
		rezDefs = [ 'r_klank', 'r_lpf',  'r_hpf',     'r_hpf', 	   // row 1 
				    'r_klank', 'r_klank','r_klank',   'r_lpf'];// row 2
		rez = Array.fill(8, { |ind|
			var rz;
			rz = DSRez.new(drumSynthGroup);
			rz.synthDef = rezDefs[ind];
			rz;
		});
		drum = Array.fill(8, { |ind|
			var dr;
			dr = DSDrum.new;
			dr.synthDef = drumDefs[ind];
			dr.groupID = drumXGroup;
			dr;
		});
		noteOns = Dictionary[50 -> 0, 45 -> 1, 51 -> 2, 49 -> 3, 36 -> 4, 38 -> 5, 46 -> 6, 42 -> 7];

		drumParams = [			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 10, 'gain' -> 0.1], // x_gray
			Dictionary['curve' -> -5, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 11,
						  'freq' -> 80, 'modPhase' -> 0, 'modFreq' -> -1, 'modAmt' -> 0, 'drive' -> 10], // x_osc
			Dictionary['curve' -> -10, 'att' -> 0.0001, 'rel' -> 0.6, 'outBus' -> 12, 
					  'crackle' -> 1.5, 'gain' -> 2], // x_crack
			Dictionary['curve' -> -5, 'att' -> 0.0001, 'rel' -> 0.9, 'outBus' -> 13, 
					  'crackle' -> 1.5, 'gain' -> 1.3], // x_crack
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 14], //x_clip
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 15], //x_clip
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 16], //x_clip
			Dictionary['curve' -> -10, 'att' -> 0.001, 'rel' -> 0.5, 'outBus' -> 17,
					   'gain' -> 3, 'freq' -> 1000, 'rez' -> 2] // x_whiteSnare
		];
		drumParams.do{ |obj,ind|
			drum[ind].params = obj;
		};
		rezParams = [
			Dictionary['f1' -> 100, 'f2' -> 80, 'f3'-> 250,
					  'r1' -> 0.1, 'r2' -> 0.5, 'r3' -> 0.8,
					  'a1' -> 0.2, 'a2' -> 0.3, 'a3' -> 0.4, 'inBus' -> 10], // r_klank
			Dictionary['freq' -> 1600, 'res' -> 10, 'gain' -> 2, 'inBus' -> 11], // r_lpf
			Dictionary['freq' -> 600, 'res' -> 10, 'inBus' -> 12], // r_hpf
			Dictionary['freq' -> 600, 'res' -> 10, 'inBus' -> 13], // r_hpf
			Dictionary['f1' -> 120, 'f2' -> 75, 'f3'-> 55,
					  'r1' -> 0.2, 'r2' -> 0.1, 'r3' -> 0.1,
					  'a1' -> 0.4, 'a2' -> 0.3, 'a3' -> 0.2, 'inBus' -> 14], // r_klank
			Dictionary['f1' -> 1200, 'f2' -> 900, 'f3'-> 900,
					  'r1' -> 0.5, 'r2' -> 0.1, 'r3' -> 0.2,
					  'a1' -> 0.3, 'a2' -> 0.4, 'a3' -> 0.4, 'inBus' -> 15], // r_klank
			Dictionary['f1' -> 80, 'f2' -> 95, 'f3'-> 145,
					  'r1' -> 0.5, 'r2' -> 0.4, 'r3' -> 0.6,
					  'a1' -> 0.4, 'a2' -> 0.3, 'a3' -> 0.2, 'inBus' -> 16], // r_klank
			Dictionary['freq' -> 2600, 'res' -> 1, 'gain' -> 2, 'inBus' -> 17] // r_lpf
		];
		rezParams.do{ |obj,ind|
			rez[ind].params = obj;
		};

		// 72  8 74 71  20 22 86 73
		drumCCCommands = Dictionary[
			50 -> Dictionary[ // x_gray r_klank
				72 ->   { |val| this.setRezParam(0, 'f1', val.midicps); },
				8  ->   { |val| this.setRezParam(0, 'f2', val.midicps); },
				74 ->   { |val| this.setRezParam(0, 'f3', val.midicps); },
				20 ->   { |val| this.setRezParam(0, 'r1', val / 1.27); },
				22 ->   { |val| this.setRezParam(0, 'r2', val / 1.27); },
				86 ->   { |val| this.setRezParam(0, 'r3', val / 1.27); },
				1000 -> { |val| this.setDrumParam(0, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(0, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(0, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setRezParam(0, 'a1', val / 1.27); },
				1004 -> { |val| this.setRezParam(0, 'a2', val / 1.27); },
				1005 -> { |val| this.setRezParam(0, 'a3', val / 1.27); }
			],  
			45  -> Dictionary[ // x_osc r_lpf
				72 ->   { |val| this.setRezParam(1, 'freq', val.midicps); },
				8  ->   { |val| this.setRezParam(1, 'res', val / 1.27); },
				20 ->   { |val| this.setDrumParam(1, 'freq', val.midicps); },
				22 ->   { |val| this.setDrumParam(1, 'modFreq', val / 1.27); },
				86 ->   { |val| this.setDrumParam(1, 'modAmt', val / 12.7); },
				73 ->   { |val| this.setDrumParam(1, 'drive', val / 6.35); },
				1000 -> { |val| this.setDrumParam(1, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(1, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(1, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setDrumParam(1, 'modPhase', val / 127); }
			], 
			51 -> Dictionary[ // x_crackle r_hpf
				72 ->   { |val| this.setRezParam(2, 'freq', val.midicps); },
				8  ->   { |val| this.setRezParam(2, 'res', val / 1.27); },
				1000 -> { |val| this.setDrumParam(2, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(2, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(2, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setDrumParam(2, 'crackle', val / 12.7); }
			], 
			49 -> Dictionary[ // x_crackle r_hpf
				72 ->   { |val| this.setRezParam(3, 'freq', val.midicps); },
				8  ->   { |val| this.setRezParam(3, 'res', val / 1.27); },
				1000 -> { |val| this.setDrumParam(3, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(3, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(3, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setDrumParam(3, 'crackle', val / 12.7); }
			], 
			36 -> Dictionary[ // x_clip r_klank
				72 ->   { |val| this.setRezParam(4, 'f1', val.midicps); },
				8  ->   { |val| this.setRezParam(4, 'f2', val.midicps); },
				74 ->   { |val| this.setRezParam(4, 'f3', val.midicps); },
				20 ->   { |val| this.setRezParam(4, 'r1', val / 1.27); },
				22 ->   { |val| this.setRezParam(4, 'r2', val / 1.27); },
				86 ->   { |val| this.setRezParam(4, 'r3', val / 1.27); },
				1000 -> { |val| this.setDrumParam(4, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(4, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(4, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setRezParam(4, 'a1', val / 1.27); },
				1004 -> { |val| this.setRezParam(4, 'a2', val / 1.27); },
				1005 -> { |val| this.setRezParam(4, 'a3', val / 1.27); }
			], 
			38 -> Dictionary[ // x_clip r_klank
				72 ->   { |val| this.setRezParam(5, 'f1', val.midicps); },
				8  ->   { |val| this.setRezParam(5, 'f2', val.midicps); },
				74 ->   { |val| this.setRezParam(5, 'f3', val.midicps); },
				20 ->   { |val| this.setRezParam(5, 'r1', val / 1.27); },
				22 ->   { |val| this.setRezParam(5, 'r2', val / 1.27); },
				86 ->   { |val| this.setRezParam(5, 'r3', val / 1.27); },
				1000 -> { |val| this.setDrumParam(5, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(5, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(5, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setRezParam(5, 'a1', val / 1.27); },
				1004 -> { |val| this.setRezParam(5, 'a2', val / 1.27); },
				1005 -> { |val| this.setRezParam(5, 'a3', val / 1.27); }
			], 
			46 -> Dictionary[ // x_clip r_klank
				72 ->   { |val| this.setRezParam(6, 'f1', val.midicps); },
				8  ->   { |val| this.setRezParam(6, 'f2', val.midicps); },
				74 ->   { |val| this.setRezParam(6, 'f3', val.midicps); },
				20 ->   { |val| this.setRezParam(6, 'r1', val / 1.27); },
				22 ->   { |val| this.setRezParam(6, 'r2', val / 1.27); },
				86 ->   { |val| this.setRezParam(6, 'r3', val / 1.27); },
				1000 -> { |val| this.setDrumParam(6, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(6, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(6, 'curve', (val / 6.35) - 10); },
				1003 -> { |val| this.setRezParam(6, 'a1', val / 1.27); },
				1004 -> { |val| this.setRezParam(6, 'a2', val / 1.27); },
				1005 -> { |val| this.setRezParam(6, 'a3', val / 1.27); }
			], 
			42 -> Dictionary[ // x_whiteSnare r_lpf
				72 ->  	{ |val| this.setDrumParam(7, 'freq', val.midicps); },
				8  ->   { |val| this.setDrumParam(7, 'rez', val / 127); },
				74 ->   { |val| this.setDrumParam(7, 'att', val / 127); },
				1001 -> { |val| this.setDrumParam(7, 'rel', val / 127); },
				1002 -> { |val| this.setDrumParam(7, 'curve', (val / 6.35) - 10); }
			]
		];
		this.initGUI;
		this.loadAllRez;
		this.initLooper;
		this.addMixerChannel;
	}
	noteOn { |src,chan,num,vel|
		if(noteOns[num] != nil){
			drum[noteOns[num]].load(vel / 127);
			lastNote = num;		
		};
	}
	cc { |src,chan,num,val|
		drumCCCommands[lastNote][num].value(val);
		[num, val].postln;
	}
	loadAllRez {
		rez.do{ |obj,ind|
			obj.load;
		};
	}
	releaseAllRez {
		rez.do { |obj,ind|
			obj.release;
		}
	}
	setDrumParam { |voice,key,val|
		drum[voice].params[key] = val;
	}
	setRezParam { |voice,key,val|
		rez[voice].params[key] = val;
		rez[voice].setParam;
	}
	initGUI {
		var win, dspvHeights, dsParamViews, paramNumSliders, paramSliders, drumNoteNumbers, drumCommandNumbers;
		
		win = GUI.window.new("DrumSynth Controls", Rect.new(100, 100, 950, 440));
		win.view.decorator = VFlowLayout(win.view.bounds);
		dspvHeights = [186, 155, 93, 93, 186, 186, 186, 93];
		dsParamViews = Array.fill(8, { |ind|
			GUI.vLayoutView.new(win, Rect.new(0, 0, 250, dspvHeights[ind]))
			    .background_(Color.new255(200, 180, 180));
			});
	
		// make the sliders //
		paramNumSliders = [12, 10, 6, 6, 12, 12, 12, 6];
		paramSliders = Array.fill(paramNumSliders.size, { |ind|
			Array.fill(paramNumSliders[ind], { |i|
				var slider;
				slider = GUI.slider.new(dsParamViews[ind], Rect.new(0, 0, 250, 12.3))
					.background_(Color.new255(100,240,100))
					/*.knobColor_(Color.white)*/;
				if(i < 3){
					slider.background = Color.red;
				};
				slider;
			});
		});
				// slider actions //
		drumNoteNumbers = [50, 45, 51, 49, 36, 38, 46, 42];
		drumCommandNumbers = [
			[1000, 1001, 1002, 72, 8, 74, 20, 22, 86, 1003, 1104, 1005], 
			[1000, 1001, 1002, 72, 8, 20, 22, 86, 73, 1003], 
			[1000, 1001, 1002, 72, 8, 1003], 
			[1000, 1001, 1002, 72, 8, 1003], 
			[1000, 1001, 1002, 72, 8, 74, 20, 22, 86, 1003, 1104, 1005], 
			[1000, 1001, 1002, 72, 8, 74, 20, 22, 86, 1003, 1104, 1005], 
		    [1000, 1001, 1002, 72, 8, 74, 20, 22, 86, 1003, 1104, 1005], 			[1000, 1001, 1002, 72, 8, 74]
		];
		paramSliders.do{ |obj,ind|
			obj.do{ |o,i|
				o.action = { |s|
					postln([drumNoteNumbers[ind], drumCommandNumbers[ind][i], s.value * 127]);
					drumCCCommands[drumNoteNumbers[ind]][drumCommandNumbers[ind][i]].value(s.value * 127);
				};
			};
		};
		
		win.front;
	}
	initLooper {
		~eventLooper.addChannel(0, recorderID);
		~eventLooper.channels[recorderID].action = { |values, index|
			this.noteOn(nil, nil, values[0], values[1]);
		};
	}
	looper {
		^~eventLooper.channels[recorderID];
	}
	addMixerChannel {
		~mixer.addMonoChannel("drumSynth", ~mixer.mixGroup);
		postln("adding a mixerChannel for the drumSynth. the outbus is the inbus of the mixer channel, which is this: " ++ ~mixer.channels["drumSynth"].inBus);
		rez.size.do{ |ind|
			rez[ind].setOutBus(~mixer.channels["drumSynth"].inBus);
		};
	}
}
*/


