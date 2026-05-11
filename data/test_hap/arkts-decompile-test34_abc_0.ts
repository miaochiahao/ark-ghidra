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

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v8;
        v2.inner = v9;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
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

    static dayType(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 0)) {
            const v5: boolean = v5 === 0;
            if (!(v5 === 1)) {
                const v5: boolean = v5 === 1;
                if (!(v5 === 2)) {
                    const v5: boolean = v5 === 2;
                    if (!(v5 === 3)) {
                        const v5: boolean = v5 === 3;
                        if (!(v5 === 4)) {
                            const v5: boolean = v5 === 4;
                            if (!(v5 === 5)) {
                                const v5: boolean = v5 === 5;
                                return "saturday";
                            }
                            return "friday";
                        }
                        return "thursday";
                    }
                    return "wednesday";
                }
                return "tuesday";
            }
            return "monday";
        }
        return "sunday";
    }

    static lock(param_0, param_1, param_2): void     {
        const v4 = v2;
        if (true) {
            const v4 = true;
            v4.locked = true;
            return true;
        }
        return false;
    }

    static tryOp(param_0, param_1, param_2, param_3): void     {
        const v6 = v2;
        const lock = v6.lock;
        v6.lock();
        if ({  }) {
            const lock = {  };
            return `${lock}:busy`;
        }
        const v6 = v3;
        const lock: string = `${v6}:ok`;
        const v7 = v2;
        v7.unlock();
        return lock;
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
        lex_0_0 = func_main_0;
        v10 = onForeground;
        lex_0_4 = v10;
    }

    static testMutex(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.tryOp("a");
        tryOp = v9.tryOp(v10);
        v9 = acc;
        tryOp = v9.tryOp;
        v10 = "b";
        v9.tryOp(v10);
        tryOp = v9.tryOp(v10);
        v9 = acc;
        v9.lock();
        v9 = acc;
        tryOp = v9.tryOp;
        v10 = "c";
        v9.tryOp(v10);
        tryOp = v9.tryOp(v10);
        v9 = acc;
        let unlock = v9.unlock;
        v9.unlock();
        v10 = `${tryOp2},`;
        v9 = v10 + tryOp3;
        unlock = `${v9},`;
        return unlock + tryOp4;
    }

    static treeDepth(param_0, param_1, param_2, param_3): void     {
        const v7 = v3;
        if (!(v7 === null)) {
            let isNull: boolean = v7 === null;
            const v8 = v3;
            let left = v8.left;
            let null = isNull(left);
            isNull = lex_0_4;
            left = v3;
            const right = left.right;
            null = isNull(right);
            isNull = _null;
            if (!(isNull > _null2)) {
                const has_null2: boolean = isNull > _null2;
                return has_null2 + 1;
            }
            const has_null2 = _null;
            return has_null2 + 1;
        }
        return 0;
    }

    static unlock(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.locked = false;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testDayType(param_0, param_1, param_2): void     {
        let v10 = lex_0_0;
        v10 = v10(0);
        let v8: string = `${v10},`;
        v10 = lex_0_0;
        v10 = 3;
        let v7: number = v8 + v10(v10);
        let v6: string = `${v7},`;
        v7 = lex_0_0;
        v8 = 6;
        let v5: number = v6 + v7(v8);
        const v4: string = `${v5},`;
        v5 = lex_0_0;
        v6 = 9;
        return v4 + v5(v6);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testTreeDepth(param_0, param_1, param_2): void     {
        let v10 = 1;
        const v7 = new 1();
        v10 = 2;
        const v4 = new 2(v9, v10);
        v10 = 3;
        const v6 = new 3(v9, v10, v11, v12);
        v10 = 4;
        const v5 = new 4(v9, v10, v11, v12, v13, v14);
        let v9 = v7;
        v9.left = v4;
        v9 = v7;
        v9.right = v6;
        v9 = v4;
        v9.left = v5;
        v9 = lex_0_4;
        v9 = v9(v7);
        v9 = String;
        return v9(v9);
    }

    static testMultiArray(param_0, param_1, param_2): void     {
        const v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v14;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v15;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v16;
        const v3: Array<unknown> = v6;
        v7 = 0;
        do {
            const v9 = v3;
            let v10 = v7;
            const v8 = v9[v10];
            v10 = 0;
        } while (0);
        const v11 = 0;
        const v12 = v8;
        while (!(v11 < v12.length)) {
            loop_0: do {
    let hasLength: boolean = v11 < v12.length;
    let v12 = v8;
} while (hasLength < v12.length);
        }
    }

    static getDeepValue(param_0, param_1, param_2): void     {
        const v6 = v2;
        const middle = v6.middle;
        const inner = middle.inner;
        return inner.value;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testNestedAccess(param_0, param_1, param_2): void     {
        let v9 = 42;
        v9 = new 42();
        v9 = new new 42()(v8, v9);
        v9 = new new new 42()(v8, v9)(v8, v9, v10, v11);
        let deepValue = v9.getDeepValue;
        v9.getDeepValue();
        deepValue = v9.getDeepValue();
        deepValue = String;
        v9 = deepValue2;
        return deepValue(v9);
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

    static testBooleanShortCircuit(param_0, param_1, param_2): void     {
        const v3 = true;
        const v4 = false;
        { }
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