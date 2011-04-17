#ifndef IR_H_
#define IR_H_

#include <stdbool.h>

/*
 * \return bool
 * \retval true		running learn mode
 * \retval false	done running learn mode
 */
bool ir_learn(int us);

/*
 */
bool ir_main(int ms);

#endif /*IR_H_*/

