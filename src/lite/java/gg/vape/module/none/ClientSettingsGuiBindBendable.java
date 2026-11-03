package gg.vape.module.none;

import com.google.common.collect.ImmutableList;
import gg.vape.Vape;
import gg.vape.input.BindSet;
import gg.vape.module.Mod;
import gg.vape.unmap.ModBendable;
import java.util.Collections;
import java.util.List;

public class ClientSettingsGuiBindBendable
extends ModBendable {

    public ClientSettingsGuiBindBendable(Mod mod) {
        super(mod);
    }

    @Override
    public boolean usesOwnKeybindStorage() {
        return false;
    }

    @Override
    public boolean supportsActivationMode() {
        return false;
    }

    @Override
    public List<Integer> getBoundInputs() {
        List<Integer> boundInputs = ((BindSet)Vape.INSTANCE.getPublicProfileSettings().guiBind.getValue()).getBoundInputs();
        if (boundInputs.contains(25)) {
            List<Integer> compatibleInputs = new java.util.ArrayList<Integer>(boundInputs);
            compatibleInputs.remove(Integer.valueOf(25));
            if (!compatibleInputs.contains(80)) {
                compatibleInputs.add(80);
            }
            return ImmutableList.copyOf(compatibleInputs);
        }
        return ImmutableList.copyOf(boundInputs);
    }

    @Override
    public void setBoundInputs(List<Integer> inputCodes) {
        ((BindSet)Vape.INSTANCE.getPublicProfileSettings().guiBind.getValue()).setBoundInputs(inputCodes);
        if (!this.hasValidBinding()) {
            this.setBoundInputs(Collections.singletonList(80));
        }
    }
}
