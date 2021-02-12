package de.fff.ccgt;

/*
    Philip McLeod's Tartini Method:

For each frame {
    • Read in hop size new input samples and append them to x.
    • Update y; the array of filtered values from x, using the
      middle/outer filter from Section 6.2.2.
    • Calculate the Modified Cepstrum, from Section 6.4.1, using a
      window that consists of the latest W values from y.
    • Warp the result and it add to the warped aggregate lag domain.
    • Use the peak picking algorithm, from Section 6.3, on the
      aggregate lag domain to to find the pitch period estimate, P p .
      Note that Equation 6.3 is used here for the peak thresholding.
    • Calculate the SNAC or WSNAC function using the latest values from y.
      The window size can be W or less.
    • Find and store all the primary-peaks using parabolic interpolation.
    • Update the chosen primary-peak for every frame in current note using
      the new P p - giving the pitch values at the effective centre of each
      frame in the note so far.
    • if(doing detailed pitch analysis) {
        - Perform the Incremental SNAC or WSNAC using a small window
        based on the pitch period.
        - Smooth the pitch MIDI number values with a Hanning window
        CMA filter.
    }
    • Calculate the vibrato parameters using the Prony method variant
      from Section 9.3.
    • Calculate the short-term and long-term pitch mean-pitch as
      discussed in Section 7.3.1.
    • if(note is ending or transitioning) {
        - Do back-tracking (Section 7.3.2).
        - Update the chosen peaks in the finished note.
        - if(a new note has began) {
            - Perform forward-tracking (Section 7.3.3).
            - Update chosen peaks in the new note.
        }
    }
    • Update the display if required.
}
*/



public class PitchTracker {
    //guess this should extend thread





}
