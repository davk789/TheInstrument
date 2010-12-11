
// variables

~startAngle = 0.975;
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
    .background_(Color.black)
    .mouseDownAction_({ |obj| obj.refresh })
    .mouseMoveAction_({ |obj| obj.refresh });

~view.drawFunc = {
	var lineStartPoint, lineEndPoint;
	Pen.use{
		// options

		Pen.strokeColor = Color.white;
		Pen.fillColor = Color.black;
		Pen.width = 5;

		// top right circle

		Pen.addArc(~circleAOrigin, ~circleASize, (~startAngle) * 2pi, (~startAngle - 0.06) * 2pi);

		// bottom left circle
		Pen.moveTo(~circleBOrigin + (~circleBSize @ 0));
		Pen.addArc(~circleBOrigin, ~circleBSize, 0, 2pi);

		// connecting line
		lineStartPoint = ~convertToPolar.value(~circleASize, 0.375 * 2pi) + ~circleAOrigin;
		lineEndPoint = ~convertToPolar.value(~circleBSize, 0.9 * 2pi) + ~circleBOrigin;

		Pen.moveTo(lineStartPoint);
		Pen.lineTo(lineEndPoint);

		// hairs

		(10.rand + 5).do{ |ind|
			var angle;
			var radialDistance = 0;
			
			Pen.moveTo(~circleBOrigin);
			
			angle = (0.8.rand * 2pi) - 0.025;

			Pen.moveTo(~convertToPolar.value(~circleBSize, angle) + ~circleBOrigin);
			
			(10.rand + 5).do{ |ind|
//				Pen.strokeColor = Color.rand;
				
				radialDistance = 100.rand + radialDistance;
				
				angle = angle + (0.01.rand - 0.005);
				
				Pen.lineTo(~convertToPolar.value(radialDistance + ~circleBSize, angle + 0.1.rand) + ~circleBOrigin);
			}
			
		};

		//

		Pen.stroke;


	};
};


/*
	// alternate draw function -- per segment changes to the fingers
~view.drawFunc = {
	var lineStartPoint, lineEndPoint;
	Pen.use{
		// options

		Pen.strokeColor = Color.white;
		Pen.fillColor = Color.black;
		Pen.width = 5;

		// top right circle

		Pen.addArc(~circleAOrigin, ~circleASize, (~startAngle) * 2pi, (~startAngle - 0.06) * 2pi);

		// bottom left circle
		Pen.moveTo(~circleBOrigin + (~circleBSize @ 0));
		Pen.addArc(~circleBOrigin, ~circleBSize, 0, 2pi);

		// connecting line
		lineStartPoint = ~convertToPolar.value(~circleASize, 0.375 * 2pi) + ~circleAOrigin;
		lineEndPoint = ~convertToPolar.value(~circleBSize, 0.9 * 2pi) + ~circleBOrigin;

		Pen.moveTo(lineStartPoint);
		Pen.lineTo(lineEndPoint);

		Pen.stroke;

		// hairs

		(10.rand + 5).do{ |ind|
			var angle;
			var radialDistance = 0;
			var oldLocation;
			
			Pen.moveTo(~circleBOrigin);
			
			angle = (0.8.rand * 2pi) - 0.025;

			oldLocation = ~convertToPolar.value(~circleBSize, angle) + ~circleBOrigin;
			Pen.moveTo(oldLocation);
			
			(10.rand + 5).do{ |ind|
				Pen.width = 2 + 10.rand;

				Pen.moveTo(oldLocation);

				radialDistance = 100.rand + radialDistance;
				
				angle = angle + (0.01.rand - 0.005);
				
				oldLocation = ~convertToPolar.value(radialDistance + ~circleBSize, angle + 0.1.rand) + ~circleBOrigin;
				Pen.lineTo(oldLocation);
				Pen.stroke;
			};
				
		};

		//

		Pen.stroke;


	};
};
*/
// WFSMarkerArea SCPen Document
/*
//warning infinite loop !!! Do not uncomment!!!
Document.current.path.load;
*/