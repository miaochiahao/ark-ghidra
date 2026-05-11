export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v8 = "testTag";
        const v9 = "Failed to load the content. Cause: %{public}s";
        const v11 = JSON;
        v11.stringify(v16);
        stringify = v11.stringify(v12);
        v6.error(v7, v8, v9);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        try {
        } catch (e) {
            const error = v8.error;
            const v10 = "testTag";
            const v11 = "Failed to set colorMode. Cause: %{public}s";
            const v13 = JSON;
            let stringify = v13.stringify;
            const v14 = v6;
            v13.stringify(v14);
            stringify = v13.stringify(v14);
            v8.error(v9, v10, v11);
        }
        const v9 = "testTag";
        const v10 = "%{public}s";
        const v11 = "Ability onCreate";
        error.info(v8, v9, v10);
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v9 = "%{public}s";
        v6.info(v7, v8, v9);
        const v6 = v3;
        const v7 = "pages/Index";
        v8 = onBackground;
        v6.loadContent(v7, v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        try {
            throw v5;
        } catch (e) {
            throw v3;
        }
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        try {
            throw v6;
        } catch (e) {
            throw v4;
        }
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2)     {
        v2 = v7;
        v2.resolved = false;
        v4 = v2;
        v4.value = undefined;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v4;
        return v11 * v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        v7.create(lex_0_0.message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v18;
        lex_0_2 = func_main_0;
        v10 = anonymous_method;
        lex_0_1 = v10;
        v10 = func_main_0;
        lex_0_4 = v10;
        v10 = onCreate;
        lex_0_0 = v10;
        let v12 = ViewPU;
        let v12 = Reflect;
        let set = v12.set;
        v12.set(ViewPU.prototype, "finalizeConstruction", testRadix);
        prototype = undefined;
        let v0: @system.app = @system.app;
        v12 = undefined / set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v12 = undefined / set;
        prototype = set.prototype;
        v15 = "message";
        let v16 = undefined;
        false[v17] = "get".v16;
        v15 = "message";
        v16 = undefined;
        v17 = testDeferred;
        false[v16] = "get".v17;
        prototype2.initialRender = anonymous_method;
        prototype2.rerender = testRepeatChar;
        set.getEntryName = updateStateVars;
        set = set;
        set = set;
        set = registerNamedRoute;
        v12 = func_44;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static padCenter(param_0, param_1, param_2, param_3, param_4): void     {
        let v11 = Math;
        let floor = v11.floor;
        const v13 = v4;
        const v14 = v3;
        let v12: number = v14.length - v13;
        v12 = 2 / v12;
        v11.floor(v12);
        floor = v11.floor(v12);
        v11 = v19;
        v12 = v18;
        floor = v12.length - v11;
        const v5 = "";
        v11 = 0;
        do {
            let v12 = v5;
            const v5: string = ` ${v12}`;
            v12 = v11;
            const number: number = Number(v12);
            let v11: number = _number + 1;
        } while (_number);
        let v11 = 0;
        const v8: string = ` ${_number}`;
        _number = v11;
        const number: number = Number(_number);
        let v11: number = _number + 1;
        for (let v8 = ""; !(v7 < _number); _number) {
            let _number = v11;
        }
        let v11 = _number;
        let floor: number = v3 + v11;
        return v8 + floor;
    }

    static testRadix(param_0, param_1, param_2): void     {
        const v6 = 0xFF;
        let v10: number = v6;
        let toString = v10.toString;
        v10.toString(16);
        toString = v10.toString(v11);
        v10 = v6;
        toString = v10.toString;
        v11 = 2;
        v10.toString(v11);
        toString = v10.toString(v11);
        v10 = v6;
        toString = v10.toString;
        v11 = 8;
        v10.toString(v11);
        toString = v10.toString(v11);
        v10 = v6;
        toString = v10.toString;
        v11 = 10;
        v10.toString(v11);
        toString = v10.toString(v11);
        let v14 = toString2;
        const v13: string = `,${v14}`;
        v14 = toString3;
        v11 = `,${v14.length + v13}`;
        v10 = toString4 + v11;
        toString = `,${v10}`;
        return toString5 + toString;
    }

    static maxOfArray(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        let v7 = 0;
        const v4 = v6[v7];
        v7 = 1;
        const v8 = v7;
        const v9 = v3;
        while (!(v9.length < v8)) {
            const v9 = v3;
            const v10 = v7;
            const v8 = v9[v10];
            if (v4 > v8) {
                const v8 = v3;
                const v9 = v7;
                const v4 = v8[v9];
                const v8 = v7;
                const number: number = Number(v8);
                let v7: number = _number + 1;
                continue;
            }
        }
        return v4 > v8;
    }

    static minOfArray(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        let v7 = 0;
        const v4 = v6[v7];
        v7 = 1;
        const v8 = v7;
        const v9 = v3;
        while (!(v9.length < v8)) {
            const v9 = v3;
            const v10 = v7;
            const v8 = v9[v10];
            if (v4 < v8) {
                const v8 = v3;
                const v9 = v7;
                const v4 = v8[v9];
                const v8 = v7;
                const number: number = Number(v8);
                let v7: number = _number + 1;
                continue;
            }
        }
        return v4 < v8;
    }

    static repeatChar(param_0, param_1, param_2, param_3, param_4): void     {
        v3 = v13;
        v4 = v14;
        const v8 = 0;
        v3 = v13;
        v4 = v14;
        const v8 = 0;
        v9 = v8;
        const number: number = Number(v9);
        const v8: number = _number + 1;
        for (v0 = v10; !(v4 < v9); v9) {
            let v9 = v8;
        }
        return _number;
    }

    static testMinMax(param_0, param_1, param_2): void     {
        let v7: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v7 = lex_0_1;
        v7 = v7(v3);
        v7 = lex_0_2;
        v7 = v7(v3);
        let v9 = String;
        v9 = v9(v7);
        v7 = `,${v9}`;
        v9 = String;
        return v72(v72) + v7;
    }

    static resolve(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.resolved = true;
        v5 = v2;
        v5.value = v9;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testPadding(param_0, param_1, param_2): void     {
        let v5 = lex_0_4;
        v5 = v5("hi", 10);
        v7 = String;
        v7 = v7(v5.length);
        v5 = `:${v7}`;
        return v5 + v5;
    }

    static getValue(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.value;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testDeferred(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.isResolved();
        resolved = v9.isResolved();
        v9 = acc;
        v9.resolve("hello");
        v9 = acc;
        resolved = v9.isResolved;
        v9.isResolved();
        resolved = v9.isResolved();
        v9 = acc;
        let value = v9.getValue;
        v9.getValue();
        value = v9.getValue();
        let v12 = String;
        v12 = v12(resolved2);
        v10 = `,${v12}`;
        v12 = String;
        v12 = resolved3;
        v9 = v12(v12) + v10;
        value = `,${v9}`;
        v9 = String;
        v10 = value2;
        return v9(v10) + value;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testArrayFrom(param_0, param_1, param_2): void     {
        let v7 = Array;
        let v8 = {  };
        v8 = v8;
        v7.from(v8, onDestroy);
        from = v7.from(v8, v9);
        const v4 = 0;
        v7 = 0;
        do {
            let v8 = v4;
            const v9 = from2;
            const v10 = v7;
            const v4: number = v9[v10] + v8;
            v8 = v7;
            const number: number = Number(v8);
            let v7: number = _number + 1;
        } while (_number);
    }

    static isResolved(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.resolved;
    }

    static testRepeatChar(param_0, param_1, param_2): void     {
        let v7 = lex_0_0;
        v7 = v7("*", 5);
        v7 = lex_0_0;
        v8 = "-";
        v9 = 3;
        v7 = v7(v8, v9);
        v7 = lex_0_0;
        v8 = ".";
        v9 = 10;
        v7 = v7(v8, v9);
        let v11 = String;
        v11 = v11(v7.length);
        v9 = `,${v11}`;
        v11 = String;
        length = v72.length;
        v8 = v72(length2) + v9;
        v7 = `,${v8}`;
        v8 = String;
        length = v73.length;
        return v8(length3) + v7;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5.observeComponentCreation2(func_main_0, Column);
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5.observeComponentCreation2(v6, v7);
        v5 = Text;
        v5.pop();
        v5 = Column;
        pop = v5.pop;
        v5.pop();
        return;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.aboutToBeDeleted();
        let v7 = SubscriberManager;
        v7.Get();
        get = v7.Get();
        let id__ = v7.id__;
        v10.id__();
        id__ = v10.id__();
        get2.delete(id__);
        const get2 = v2;
        get2.aboutToBeDeletedInternal();
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const message = v6.message;
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.purgeDependencyOnElmtId(v11);
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}