


postln("hello i am a real file");



// ProxySpace returns instances of NodeProxy:
a = NodeProxy(s) is equivalent to  ~a;
a.source = ... is equivalent to  ~a = ...
a[3] = ... is equivalent to  ~a[3] = ...

// the two expressions are equivalent: 
~out = something;   
currentEnvironment.put(\out, something);

// examples

p = ProxySpace.push(s.boot); // use ProxySpace as current environment.

~out.play;

~out = { SinOsc.ar([400, 407] * 0.9, 0, 0.2) };

~out = { SinOsc.ar([400, 437] * 0.9, 0, 0.2) * LFPulse.kr([1, 1.3]) };

~out = { SinOsc.ar([400, 437] * 0.9, 0, 0.2) * ~x.kr(2) };

~x = { LFPulse.kr([1, 1.3] * MouseX.kr(1, 30, 1)) };

~out = { SinOsc.ar([400, 437] * Lag.kr(0.1 + ~x, 0.3), 0, 0.2) * ~x };

p.fadeTime = 5;

~out = { SinOsc.ar([400, 437] * 1.1, 0, 0.2) * ~x.kr(2) };

p.clear(8); // end and clear all in 8 sec.

p.pop; // move out.

