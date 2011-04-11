#include "ir.h"
#include "hw.h"

#if 0
void ir_learn(void)
{
	enum state {
		default_state = 0,
		get_pkt_1 = 0,
		get_pkt_1_tailer,
		get_pkt_2
	};
	
	enum state current_state = default_state;
	bool scan_codes = true;
	uint16_t c = 0;
	
	/*Initiate learn*/
	i = 0;
	P1IE |= BIT3;	/* Enable IR_IN interrupt */
	get_us();       /* Clear microsecond counter */	
	
	while (scan_codes) {
		while ((c = ir_getint()) != EOF) {
			switch (current_state) {
			case get_pkt_1:	/*Get the first pause between packets*/			
				if (c > 20000) {
					current_state = get_pkt_1_tailer;					
				}
				break;
			case get_pkt_1_tailer:	/*Get the second pause between packets*/
				if (c > 20000) {
					current_state = get_pkt_2;					
				}
				break;
			case get_pkt_2:
				if (c > 20000) {
					scan_codes = false;					
				} else {
					buf[i++] = c;
					i %= sizeof(buf);
				}
				
				break;
			default:
				current_state = default_state;
				break;
			}
		}
	}
	P1IE &= ~BIT3;	/* Disable IR_IN interrupt */		
}
#endif

bool ir_learn(int us)
{
    enum state {
        default_state = 0,
        rx_pulses_find_pkt = 0,
        rx_spaces_find_pkt1,
        rx_spaces_find_pkt2,
        rx__find_pkt2,
        rx_pulses,
        rx_spaces
    };
    static enum state current_state = default_state;
    static int duration = 0;
    static bool found_pkt = false,
                found_pkt1 = false;
    bool run_again = true;

    if (!us) {
        return run_again;
    }

    switch (current_state) {
    case rx_pulses_find_pkt:
        if (!got_pulse) {
            duration = 0;
            if (!found_pkt1) {
                current_state = rx_spaces_find_pkt1;
            } else {
                current_state = rx_spaces_find_pkt2;
            }
        } else {
            got_pulse = false;
        }
        break;

    case rx_spaces_find_pkt1:
        if (!got_pulse) {
            duration += us;
            if (duration > MAX_SPACE_WAIT_TIME) {
                found_pkt1 = true;
            }
        } else {
            duration = 0;
            current_state = rx_pulses_find_pkt;
        }
        break;

    case rx_spaces_find_pkt2:
        if (!got_pulse) {
            duration += us;
            if (duration > MAX_SPACE_WAIT_TIME) {
                found_pkt = true;
            }
        } else {
            duration = 0;
            current_state = rx_pulses;
        }
        break;

    case rx_pulses:
        if (got_pulse) {
            duration += us;
            got_pulse = 0;
        } else {
            (void)buf_enque(&ir_rx, *(uint8_t *)(&duration));
            (void)buf_enque(&ir_rx, *(uint8_t *)(&duration + 1));
            duration = 0;
            current_state = rx_spaces;
        }
        break;

    case rx_spaces:
        if (!got_pulse) {
            duration += us;
            if (duration > MAX_SPACE_WAIT_TIME) {
                run_again = false;
                duration = 0;
                found_pkt = false;
				found_pkt1 = false;
                current_state = default_state;
            }
        } else {
            (void)buf_enque(&ir_rx, *(uint8_t *)(&duration));
            (void)buf_enque(&ir_rx, *(uint8_t *)(&duration + 1));
            duration = 0;
            current_state = rx_pulses;
        }
        break;

    default:
        current_state = default_state;
        duration = 0;
        found_pkt = false;
		found_pkt1 = false;
        run_again = false;
        break;
    }

    return run_again;
}


bool ir_main(int us)
{
	bool run_again = true;
	
	return run_again;
}
