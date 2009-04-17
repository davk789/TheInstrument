MonoSequencer {
	var <>dur=0.15, <>length=0.1, <>onsetAction, <>releaseAction, prSequence, index=0, <>clock,
		noteView, velocityView;
	*new {
		^super.new.init_monosequencer;
	}
	init_monosequencer { 
		onsetAction = { |...args| args.postln; };
		releaseAction = { |...args| args.postln; };
		prSequence = Array.new;
		clock = TempoClock.new;
		this.makeGUI;
	}
	startNew {
		clock.sched(0, {
		 var dur, params;
		 params = this.getNext[0];
		 onsetAction.(params[1], params[2]);
		 clock.sched(~length, {
		 	releaseAction.(params[1], 0);
		 	nil;
		 });
		 dur = 
		 ~dur;
		});
	}
	getNext { |sel|
		 var ret;
		 ret = prSequence[index % prSequence.size];
		 index = index + 1;
		 ^ret;
	}
	sequence_ { |seq|
	
	}
	seqence {
		^prSequence;
	}
	makeGUI {
		var win;
		win = GUI.window.new("MonoSequencer", Rect.new(0, 0, 400, 300)).front;
		win.view.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
		
	}
	
}
/*

MIDIClient.init;

m = MIDIOut.new(0,MIDIClient.destinations[6].uid);

~dur = 0.15;
~tempDur = ~dur;
~length = 0.05;
~root = 59;
/*~scale = [
 12, 0, 24, 12, 33, 21, 23, 33, 
 19, 0, 24, 19, 33, 21, 23, 33, 
 14, 0, 28, 14, 33, 21, 23, 33];*/
~scale1 = Array.fill(1000, { |ind|
 var rnd, rnd2;
 rnd = 3.rand * 7;
 rnd2 = 3.rand * 7;
 [22 + rnd2, 0, 24, rnd, 33, 21 + rnd2, 13, 3, 4, 6 + rnd2, 7, 8, 9, 9 + rnd2, 4, 3, 2, 1, 5 + rnd];
}).flatten;
~scale2 = Array.fill(1000, { |ind|
 var rnd, rnd2;
 rnd = (5.rand - 0.25) * 5;
 rnd2 = (5.rand - 0.25) * 5;
 [15, 25, 35 + rnd2, 45 + rnd, 35 + rnd2, 25 + rnd, 15, rnd2 + 5, 5, 25, 45, 65, 45, 25, 15, 5];
}).flatten;
~scale = [43, 28, 16, 28];
~scale = [~scale1, ~scale2].choose;
~ind = 0;
~durInd = 0;
~getNote = {
 var ret;
 ret = ~scale[~ind % ~scale.size] + ~root;
 ~ind = ~ind + 1;
 ret;
};

~count = 25;
~setDur = {
 if(~durInd < ((4.rand * 6) + 5)){
  ~dur = ~tempDur;
  ~durInd = ~durInd + 1;
 }{
  ~dur = 0.4;
  ~durInd = 0;
 };
};
TempoClock.sched(0, {
 var note;
 note = ~getNote.();
 m.noteOn(1, note, 60.rand + 60);
 TempoClock.sched(~length, {
  m.noteOff(1, note, 0);
  nil;
 });
 ~setDur.();
 ~dur;
});*/