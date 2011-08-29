#ifndef IR_H_
#define IR_H_

#include <stdbool.h>
#include <stdint.h>

/*
 * \return bool
 * \retval true		running learn mode
 * \retval false	done running learn mode
 */
bool ir_learn(int_fast32_t us);

/*
 */
bool ir_main(int_fast32_t us);

/*
 * abort the IR TX command
 */
bool ir_tx_abort();

/*
 * set the number of times to repeat an ir code
 * 0: Use the default value NBR_IR_BURSTS
 */
void set_ir_repeat_cnt(int cnt);

#endif /*IR_H_*/

