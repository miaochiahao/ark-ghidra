export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw v6;
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
        const v10 = "testTag";
        const v11 = "Failed to set colorMode. Cause: %{public}s";
        const v13 = JSON;
        v13.stringify(v6);
        stringify = v13.stringify(v14);
        v8.error(v9, v10, v11);
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
        throw v6;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3, param_4)     {
        v2 = v9;
        let v6 = v3;
        super();
        v6 = super();
        v6.code = v11;
        throw v6;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static add(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        let x = v8.x;
        const v6: number = x + v8.x;
        let v9 = v2;
        const y = v9.y;
        v9 = v13;
        x = y + v9.y;
        return new y + v9.y(v5, v6, x, y, v9, v10, v11, v12, v13, v14);
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

    static zero(param_0, param_1, param_2): void     {
        return new 0();
    }

    static testVec2(param_0, param_1, param_2): void     {
        let v11 = 3;
        let v12 = 4;
        v11 = new 4();
        v11.length();
        length = v11.length();
        v11 = v6;
        v12 = 2;
        v11.scale(v12);
        scale = v11.scale(v12);
        v11 = scale2;
        length = v11.length;
        v11.length();
        length = v11.length();
        v11 = v6;
        v12 = scale2;
        v11.add(v12);
        add = v11.add(v12);
        let zero = v11.zero;
        v11.zero();
        zero = v11.zero();
        let v16 = String;
        v16 = v16(length2);
        let v14: string = `${v16},`;
        v16 = String;
        v16 = length3;
        v12 = `${v14 + v16(v16)},`;
        v13 = String;
        v14 = add2;
        v11 = v12 + v13(v14.x);
        zero = `${v11},`;
        v11 = String;
        v12 = zero2;
        x = v12.x;
        return zero + v11(x2);
    }

    static scale(param_0, param_1, param_2, param_3): void     {
        const v8 = v2;
        let x = v8.x;
        const v6: number = x * v3;
        x = v12.y * v13;
        return new y * v13(v5, v6, x, y, v9, v10);
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
        lex_0_2 = func_main_0;
        throw acc;
    }

    static length(param_0, param_1, param_2): void     {
        const v5 = Math;
        let v8 = v2;
        const x = v8.x;
        v8 = v11;
        const v6: number = x * v8.x;
        const y = v8.y;
        v6 += y * v11.y;
        v5.sqrt(v6);
        return v5.sqrt(v6);
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static classifyAge(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 < 13)) {
            const v5: boolean = v5 < 13;
            if (!(v5 < 20)) {
                const v5: boolean = v5 < 20;
                return "adult";
            }
            return "teen";
        }
        return "child";
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testClassify(param_0, param_1, param_2): void     {
        let v10 = lex_0_2;
        v10 = v10(8);
        let v8: string = `${v10},`;
        v10 = lex_0_2;
        v10 = 16;
        let v7: number = v8 + v10(v10);
        let v6: string = `${v7},`;
        v7 = lex_0_2;
        v8 = 40;
        let v5: number = v6 + v7(v8);
        const v4: string = `${v5},`;
        v5 = lex_0_2;
        v6 = 70;
        return v4 + v5(v6);
    }

    static fromAngle(param_0, param_1, param_2, param_3): void     {
        const v8 = Math;
        v8.cos(v14);
        cos = v8.cos(v9);
        v9 = Math;
        let sin = v9.sin;
        v9.sin(v14);
        sin = v9.sin(v10);
        return new v9.sin(v10)(v5, cos2, sin2, sin, v9, v10, v11, v12, v13, v14);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testArraySlice(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v8: Array<unknown> = v3;
        let slice = v8.slice;
        v8.slice(1, 4);
        slice = v8.slice(v9, v10);
        v8 = v3;
        slice = v8.slice;
        v9 = 2;
        v8.slice(v9);
        slice = v8.slice(v9);
        let v11 = String;
        v11 = v11(slice2.length);
        v9 = `${v11},`;
        v11 = String;
        v11 = slice2;
        length = 0;
        v11 = v11[length];
        v8 = v9 + v11(v11);
        slice = `${v8},`;
        v8 = String;
        v9 = slice3;
        length = v9.length;
        return slice + v8(length2);
    }

    static testStringCombo(param_0, param_1, param_2): void     {
        const v6 = "Hello, World!";
        let v12: string = v6;
        v12.toUpperCase();
        toUpperCase = v12.toUpperCase();
        v12 = v6;
        v12.toLowerCase();
        toLowerCase = v12.toLowerCase();
        v12 = v6;
        v12.substring(7, 12);
        substring = v12.substring(v13, v14);
        v12 = v6;
        v13 = "World";
        v12.indexOf(v13);
        indexOf = v12.indexOf(v13);
        v12 = v6;
        v13 = "World";
        v14 = "ArkTS";
        v12.replace(v13, v14);
        replace = v12.replace(v13, v14);
        v12 = v6;
        let slice = v12.slice;
        v13 = 0;
        v14 = 5;
        v12.slice(v13, v14);
        slice = v12.slice(v13, v14);
        const v20 = toUpperCase2;
        const v19: string = `${v20}|`;
        const v18: number = v19 + toLowerCase2;
        let v17: string = `${v18}|`;
        let v16: number = v17 + substring2;
        const v15: string = `${v16}|`;
        v16 = String;
        v17 = indexOf2;
        v14 = v15 + v16(v17);
        v13 = `${v14}|`;
        v12 = v13 + replace2;
        slice = `${v12}|`;
        return slice + slice2;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testTypeCoercion(param_0, param_1, param_2): void     {
        const v5 = 42;
        let v8 = String;
        v8 = v8(v5);
        v8 = Number;
        v9 = v8;
        v8 = v8(v9);
        v8 = Boolean;
        v9 = v5;
        v8 = v8(v9);
        let v11 = v8;
        let v10: string = `${v11},`;
        v11 = String;
        v9 = v10 + v11(v82);
        v8 = `${v9},`;
        v9 = String;
        return v8 + v9(v83);
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

    static testErrorSubclass(param_0, param_1, param_2): void     {
        return "caught";
    }

    static testAssignPatterns(param_0, param_1, param_2): void     {
        let v3 = 1;
        v3 = v3 + 1;
        v6 = v3;
        v3 = v6 * 3;
        v6 = v3;
        v3 = v6 - 2;
        v6 = v3;
        v3 = v6 / 2;
        v6 = "x";
        v4 = `${v6}y`;
        v6 = v4;
        v4 = `${v6}z`;
        let v8 = String;
        v8 = v8(v3);
        v6 = `${v8},`;
        return v6 + v4;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testRecordTraversal(param_0, param_1, param_2): void     {
        {  }[v8] = "Alice";
        v7 = v3;
        v8 = "city";
        v7[v8] = "Shanghai";
        v7 = v3;
        v8 = "lang";
        v7[v8] = "ArkTS";
        v8 = Object;
        v8.keys(v3);
        keys = v8.keys(v9);
        v8 = 0;
        const v9 = v8;
        const v10 = keys2;
        while (!(v9 < v10.length)) {
            loop_0: do {
    let hasLength: boolean = v9 < v10.length;
    let v10 = keys2;
} while (hasLength < v10.length);
        }
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