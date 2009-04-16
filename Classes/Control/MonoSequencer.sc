MonoSequencer {
	var dur=0.15, onsetAction, releaseAction;
	*new {
		^super.new.init_monosequencer;
	}
	init_monosequencer {
		onsetAction = { || };
	}
	startNew {
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
		});
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