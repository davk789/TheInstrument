
// variables

~startAngle = 0.95;
~circleAOrigin = 375 @ 125;
~circleASize = 50;
~circleBOrigin = 165 @ 315;
~circleBSize = 85;

// functions

~convertToPolar = { |radius, angle|
	var x, y;
	x = radius * cos(angle);
	y = radius * sin(angle);
	x @ y;
};


// gui

~win = Window("sigil", Rect.new(0,0, 500, 500)).front;

~view = UserView(~win, ~win.view.bounds)
    .background_(Color.white)
    .mouseDownAction_({ |obj| obj.refresh })
    .mouseMoveAction_({ |obj| obj.refresh });

~view.drawFunc = {
	var lineStartPoint, lineEndPoint;
	Pen.use{
		// top right circle
		Pen.strokeColor = Color.black;
		Pen.fillColor = Color.white;
		Pen.addArc(~circleAOrigin, ~circleASize, ~startAngle * 2pi, (~startAngle - 0.001) * 2pi);

		// bottom left circle
		Pen.moveTo(~circleBOrigin + (~circleBSize @ 0));
		Pen.addArc(~circleBOrigin, ~circleBSize, 0, 2pi);

		// connecting line
		lineStartPoint = ~convertToPolar.value(~circleASize, 0.375 * 2pi) + ~circleAOrigin;
		lineEndPoint = ~convertToPolar.value(~circleBSize, 0.9 * 2pi) + ~circleBOrigin;

		Pen.moveTo(lineStartPoint);
		Pen.lineTo(lineEndPoint);

		// hairs

		10.do{ |ind|
			var angle;
			Pen.moveTo(~circleBOrigin);
			
			angle = (0.8.rand * 2pi) - 0.025;

			Pen.moveTo(~convertToPolar.value(~circleBSize, angle) + ~circleBOrigin);
			Pen.lineTo(~convertToPolar.value(500, angle) + ~circleBOrigin);
		};

		//

		Pen.stroke;


	};
};



// WFSMarkerArea SCPen Document
/*
warning infinite loooop !!!
Document.current.path.load;
*/