/**
 * Web Audio API Synthesizer for Pokémon TCG Pocket Vault
 * Zero-asset tactile sound effects (Hover, Click, Inspect, Order Success)
 */
class SoundEffects {
    constructor() {
        this.ctx = null;
        this.enabled = true;
    }

    init() {
        if (!this.ctx) {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (AudioCtx) {
                this.ctx = new AudioCtx();
            }
        }
    }

    playHover() {
        if (!this.enabled) return;
        this.init();
        if (!this.ctx) return;

        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc.type = 'sine';
            const now = this.ctx.currentTime;
            osc.frequency.setValueAtTime(600, now);
            osc.frequency.exponentialRampToValueAtTime(300, now + 0.05);

            gain.gain.setValueAtTime(0.015, now);
            gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.05);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start(now);
            osc.stop(now + 0.05);
        } catch (e) {
            // Audio context policy fallback
        }
    }

    playClick() {
        if (!this.enabled) return;
        this.init();
        if (!this.ctx) return;

        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc.type = 'triangle';
            const now = this.ctx.currentTime;
            osc.frequency.setValueAtTime(1200, now);
            osc.frequency.exponentialRampToValueAtTime(400, now + 0.04);

            gain.gain.setValueAtTime(0.05, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.04);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start(now);
            osc.stop(now + 0.04);
        } catch (e) {}
    }

    playInspect() {
        if (!this.enabled) return;
        this.init();
        if (!this.ctx) return;

        try {
            const now = this.ctx.currentTime;
            [523.25, 659.25, 783.99].forEach((freq, i) => {
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();

                osc.type = 'sine';
                osc.frequency.setValueAtTime(freq, now + (i * 0.04));

                gain.gain.setValueAtTime(0.04, now + (i * 0.04));
                gain.gain.exponentialRampToValueAtTime(0.0001, now + (i * 0.04) + 0.25);

                osc.connect(gain);
                gain.connect(this.ctx.destination);

                osc.start(now + (i * 0.04));
                osc.stop(now + (i * 0.04) + 0.25);
            });
        } catch (e) {}
    }

    playOrderChime() {
        if (!this.enabled) return;
        this.init();
        if (!this.ctx) return;

        try {
            const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
            const now = this.ctx.currentTime;

            notes.forEach((freq, i) => {
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();

                osc.type = 'triangle';
                osc.frequency.setValueAtTime(freq, now + (i * 0.08));

                gain.gain.setValueAtTime(0.08, now + (i * 0.08));
                gain.gain.exponentialRampToValueAtTime(0.0001, now + (i * 0.08) + 0.4);

                osc.connect(gain);
                gain.connect(this.ctx.destination);

                osc.start(now + (i * 0.08));
                osc.stop(now + (i * 0.08) + 0.4);
            });
        } catch (e) {}
    }
}

window.soundFx = new SoundEffects();
