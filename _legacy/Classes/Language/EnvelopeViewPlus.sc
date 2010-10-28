EnvelopeViewPlus : JSCEnvelopeView {
	*new {
		^super.new.init_envelopeviewplus;
	}
	init_envelopeviewplus {
		server = Server.default;
		postln(/*this.class ++ */" initialized");
	}
}