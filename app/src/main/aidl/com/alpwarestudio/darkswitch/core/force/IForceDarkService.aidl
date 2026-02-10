package com.alpwarestudio.darkswitch.core.force;

interface IForceDarkService {
    boolean setGlobalForceDark(boolean enabled);
    int runCommand(in String[] cmd);
    void destroy();
}