package net.polyv.android.player.common.utils.ui.image.glide.transform.internal;

import android.graphics.Bitmap;

import net.polyv.android.renderscript.RenderScript;

/**
 * Copyright (C) 2020 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class RSBlur {

    public static Bitmap blur(Bitmap bitmap, int radius) throws Throwable {
        return RenderScript.blur(bitmap, radius);
    }
}