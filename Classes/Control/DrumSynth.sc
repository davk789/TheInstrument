DSKlunk {
	*new {
		^super.new.init_dsklunk;
	}
	init_dsklunk {
		postln(this.class ++ " initialized");
	}
}

DSOsc {
	var <drumID, <rezID, groupID=999, server, 
		xAtt=0.001, xRel=0.5, xLev=1, xCurve, xFreq=80, 
		xModPhase=0, xModFreq, xModAmt=0, xDrive=0, xOutBus=15,
		rFreq=1600, rRes=10,
		rOutBus=0, rInBus=12, rLev=1, rGain=1;
	*new {
		^super.new.init_dsosc;
	}
	init_dsosc {
		server = Server.default;
		xCurve = -8;
		xModFreq = -1;
		this.initGUI;
		postln(this.class ++ " initialized");
	}
	startRez {
		if(rezID.isNil){ rezID = server.nextNodeID; };
		server.sendMsg('s_new', 'r_lpf', rezID, 1, groupID,
			'freq', rFreq, 'res', rRes,
			'outBus', rOutBus, 'inBus', rInBus, 'lev', rLev, 'gain', rGain);
	}
	stopRez {
		server.sendMsg('n_free', rezID);
		rezID = nil;
	}
	hit {
		drumID = server.nextNodeID;
		server.sendMsg('s_new', 'x_osc', drumID, 0, groupID,
			'att', xAtt, 'rel', xRel, 'lev', xLev, 'curve', xCurve, 'freq', xFreq, 
			'modPhase', xModPhase, 'modFreq', xModFreq, 'modAmt', xModAmt, 'drive', xDrive, 'trig', 1, 'outBus', xOutBus,
		);
	}
	initGUI { |parent|
		
	}
}

DSHiHat {
	*new {
		^super.new.init_dshihat;
	}
	init_dshihat {
		postln(this.class ++ " initialized");
	}
}

DSSnare {
	*new {
		^super.new.init_dssnare;
	}
	init_dssnare {
		postln(this.class ++ " initialized");
	}
}

// x_gray r_klank
// x_osc r_lpf
// x_crackle r_hpf
// x_crackle r_hpf
// x_clip r_klank
// x_clip r_klank
// x_clip r_klank
// x_whiteSnare r_lpf


/*

DSBase {
	var <nodeID, <>groupID=999, <>params, <>synthDef, s;
	*new {
		^super.new.init_dsbase;
	}
	init_dsbase {
		// watch for conflicts with older code & MIDI synth IDs
		s = Server.default;
		nodeID = s.nextNodeID;
	}
	setParam {
		s.listSendMsg(['n_set', nodeID] ++ params.getPairs);
	}
}

DSDrum : DSBase {
	var <>currentNode;
	*new {
		^super.new.init_dsdrum;
	}
	init_dsdrum {
		("in DSDrum groupID = " ++ groupID ++ "\n nodeID = " ++ nodeID).postln;
	}
	load { |vel|
		currentNode = s.nextNodeID;
		if(synthDef != nil){
			s.sendMsg('s_new', synthDef, currentNode, 0, groupID, 'lev', vel);
			if(params != nil){
				s.listSendMsg(['n_set', currentNode] ++ params.getPairs);
			}{
				s.sendMsg('n_set', currentNode, 'trig', 1);
			};
		};
	}
	free {
		s.sendMsg('n_free', groupID);
	}
}

DSRez : DSBase { 
	var <>outBus=20;
	*new { |dsGroup|
		^super.new.init_dsrez(dsGroup);
	}
	init_dsrez { |dsGroup|
		groupID = s.nextNodeID;
		s.sendMsg('g_new', groupID, 1, dsGroup);
		("in DSRez groupID  = " ++ groupID).postln;
	}
	load {
		if(synthDef != nil){ // make sure a default command is called
			s.sendMsg('s_new', synthDef, nodeID, 0, groupID);
			if(params != nil){
				s.listSendMsg(['n_set', nodeID, 'outBus', outBus] ++ params.getPairs);
			};			
		};
	}
	release {
		s.sendMsg('n_free', nodeID);
	}
	setOutBus { |bus|
		outBus = bus;
		s.sendMsg('n_set', nodeID, 'outBus', outBus);
	}

}

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


