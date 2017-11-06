package ninja.egg82.utils;

import ninja.egg82.enums.EasingType;

public class EasingUtil {
	//vars
	private static double pi2 = Math.PI * 2.0d;
	private static double piO2 = Math.PI / 2.0d;
	
	//constructor
	public EasingUtil() {
		
	}
	
	//public
	public static double ease(double start, double end, double totalDuration, double currentTime, EasingType type) {
		return internalEase(currentTime, start, end - start, totalDuration, type);
	}
	
	//private
	private static double internalEase(double t, double b, double c, double d, EasingType type) {
		if (type == EasingType.LINEAR) {
			return c * t / d + b;
		} else if (type == EasingType.SINE_IN) {
			return -c * Math.cos(t / d * piO2) + c + b;
		} else if (type == EasingType.SINE_OUT) {
			return c * Math.sin(t / d * piO2) + b;
		} else if (type == EasingType.SINE_IN_OUT) {
			return -c / 2.0d * (Math.cos(Math.PI * t / d) - 1.0d) + b;
		} else if (type == EasingType.QUINTIC_IN) {
			return c * (t /= d) * t * t * t * t + b;
		} else if (type == EasingType.QUINTIC_OUT) {
			return c * ((t = t / d - 1.0d) * t * t * t * t + 1.0d) + b;
		} else if (type == EasingType.QUINTIC_IN_OUT) {
			if ((t /= d / 2.0d) < 1.0d) {
				return c / 2.0d * t * t * t * t * t + b;
			}
			return c / 2.0d * ((t -= 2.0d) * t * t * t * t + 2.0d) + b;
		} else if (type == EasingType.QUARTIC_IN) {
			return c * (t /= d) * t * t * t + b;
		} else if (type == EasingType.QUARTIC_OUT) {
			return -c * ((t = t / d - 1.0d) * t * t * t - 1.0d) + b;
		} else if (type == EasingType.QUARTIC_IN_OUT) {
			if ((t /= d / 2.0d) < 1.0d) {
				return c / 2.0d * t * t * t * t + b;
			}
			return -c / 2.0d * ((t -= 2) * t * t * t - 2.0d) + b;
		} else if (type == EasingType.EXPONENTIAL_IN) {
			return (t == 0.0d) ? b : c * Math.pow(2.0d, 10.0d * (t / d - 1.0d)) + b;
		} else if (type == EasingType.EXPONENTIAL_OUT) {
			return (t == d) ? b + c : c * (-Math.pow(2.0d, -10.0d * t / d) + 1.0d) + b;
		} else if (type == EasingType.EXPONENTIAL_IN_OUT) {
			if (t == 0.0d) {
				return b;
			}
			if (t == d) {
				return b + c;
			}
			if ((t /= d / 2.0d) < 1.0d) {
				return c / 2.0d * Math.pow(2.0d, 10.0d * (t - 1.0d)) + b;
			}
			return c / 2.0d * (-Math.pow(2.0d, -10.0d * --t) + 2.0d) + b;
		} else if (type == EasingType.ELASTIC_IN) {
			if (t == 0.0d) {
				return b;
			}
			if ((t /= d) == 1.0d) {
				return b + c;
			}
			double p = d * 0.3d;
			double a = c;
			double s = p / 4.0d;
			return -(a * Math.pow(2.0d, 1.0d * (t -= 1.0d)) * Math.sin((t * d - s) * pi2 / p)) + b;
		} else if (type == EasingType.ELASTIC_OUT) {
			if (t == 0.0d) {
				return b;
			}
			if ((t /= d) == 1.0d) {
				return b + c;
			}
			double p = d * 0.3d;
			double a = c;
			double s = p / pi2 * Math.asin(c / a);
			return a * Math.pow(2.0d, -10.0d - t) * Math.sin((t * d - s) * pi2 / p) + c + b;
		} else if (type == EasingType.ELASTIC_IN_OUT) {
			if (t == 0.0d) {
				return b;
			}
			if ((t /= d / 2.0d) == 2.0d) {
				return b + c;
			}
			double p = d * (0.3d * 1.5d);
			double a = c;
			double s = p / 4.0d;
			if (t < 1.0d) {
				return -0.5d * (a * Math.pow(2.0d, 10.0d * (t -= 1.0d)) * Math.sin((t * d - s) * pi2 / p)) + b;
			}
			return a * Math.pow(2.0d, -10.0d * (t - 1.0d)) * Math.sin((t * d - s) * pi2 / p) * 0.5d + c + b;
		} else if (type == EasingType.CIRCULAR_IN) {
			return -c * (Math.sqrt(1.0d - (t /= d) * t) - 1.0d) + b;
		} else if (type == EasingType.CIRCULAR_OUT) {
			return c * Math.sqrt(1.0d - (t = t / d - 1.0d) * t) + b;
		} else if (type == EasingType.CIRCULAR_IN_OUT) {
			if ((t /= d / 2.0d) < 1.0d) {
				return -c / 2.0d * (Math.sqrt(1.0d - t * t) - 1.0d) + b;
			}
			return c / 2.0d * (Math.sqrt(1.0d - (t -= 2.0d) * t) + 1.0d) + b;
		} else if (type == EasingType.BACK_IN) {
			double s = 1.70158;
			return c * (t /= d) * t * ((s + 1.0d) * t - s) + b;
		} else if (type == EasingType.BACK_OUT) {
			double s = 1.70158;
			return c * ((t = t / d - 1.0d) * t * ((s + 1.0d) * t + s) + 1.0d) + b;
		} else if (type == EasingType.BACK_IN_OUT) {
			double s = 1.70158;
			if ((t /= d / 2.0d) < 1.0d) {
				return c / 2.0d * (t * t * (((s *= 1.525) + 1.0d) * t - s)) + b;
			}
			return c / 2.0d * ((t -= 2.0d) * t * (((s *= 1.525) + 1.0d) * t + s) + 2.0d) + b;
		} else if (type == EasingType.BOUNCE_IN) {
			return bounceIn(t, b, c, d);
		} else if (type == EasingType.BOUNCE_OUT) {
			return bounceOut(t, b, c, d);
		} else if (type == EasingType.BOUNCE_IN_OUT) {
			if (t < d / 2.0d) {
				return bounceIn(t * 2.0d, 0.0d, c, d) * 0.5d + b;
			} else {
				return bounceOut(t * 2.0d - d, 0.0d, c, d) * 0.5d + c * 0.5d + b;
			}
		} else if (type == EasingType.CUBIC_IN) {
			return c * (t /= d) * t * t + b;
		} else if (type == EasingType.CUBIC_OUT) {
			return c * ((t = t / d - 1.0d) * t * t + 1.0d) + b;
		} else if (type == EasingType.CUBIC_IN_OUT) {
			if ((t /= d / 2.0d) < 1.0d) {
				return c / 2.0d * t * t * t + b;
			}
			return c / 2.0d * ((t -= 2.0d) * t * t + 2.0d) + b;
		}
		
		return 0.0d;
	}
	private static double bounceIn(double t, double b, double c, double d) {
		return c - bounceOut(d - t, 0.0d, c, d) + b;
	}
	private static double bounceOut(double t, double b, double c, double d) {
		if ((t /= d) < (1.0d / 2.75d)) {
			return c * (7.5625d * t * t) + b;
		} else if (t < (2.0d / 2.75d)) {
			return c * (7.5625d * (t -= (1.5d / 2.75d)) * t + 0.75d) + b;
		} else if (t < (2.5d / 2.75d)) {
			return c * (7.5625d * (t -= (2.25d / 2.75d)) * t + 0.9375) + b;
		} else {
			return c * (7.5625d * (t -= (2.65d / 2.75d)) * t + 0.984375d) + b;
		}
	}
}
