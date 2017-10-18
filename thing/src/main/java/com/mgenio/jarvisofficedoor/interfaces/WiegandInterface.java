package com.mgenio.jarvisofficedoor.interfaces;

/**
 * Created by Austin Nelson on 2/15/2017.
 */

public interface WiegandInterface {
    void onKeypadWiegandRecieved(String data);
    void onCardWiegandRecieved(String facility, String card);
}
