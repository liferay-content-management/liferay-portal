/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	MutableRefObject,
	RefObject,
	useCallback,
	useContext,
	useEffect,
	useRef,
} from 'react';
import {type DropTargetMonitor, useDrop} from 'react-dnd';
import {NativeTypes} from 'react-dnd-html5-backend';

import DnDContext from '../DnDContext';
import isFileDropEnabled from '../utils/isFileDropEnabled';
import {FDS_ITEM_DRAG_TYPE} from './useFDSDrag';

const dropTargetClass: string = 'drop-target';

const useFDSDrop = ({
	item,
	targetDropRef,
	targetDropRefQuerySelector,
}: {
	item?: any;
	targetDropRef?: RefObject<HTMLElement>;
	targetDropRefQuerySelector?: string;
}) => {
	const {fileDropSettings, handleFileDrop, handleItemDrop} =
		useContext(DnDContext);

	const targetDropElementRef: MutableRefObject<HTMLElement | null> =
		useRef<HTMLElement>(null);

	const nonDroppableRef: MutableRefObject<null> = useRef(null);

	const fileDropActive = isFileDropEnabled(fileDropSettings);

	const itemDropActive = Boolean(fileDropSettings?.onItemDrop);

	const dropActive = fileDropActive || itemDropActive;

	const isDropTarget = useCallback(
		(item?: any) => {
			if (!item) {
				return true;
			}

			return fileDropSettings?.isDropTarget
				? fileDropSettings.isDropTarget({item})
				: true;
		},
		[fileDropSettings]
	);

	const acceptedTypes: string[] = [];

	if (fileDropActive) {
		acceptedTypes.push(NativeTypes.FILE);
	}

	if (itemDropActive) {
		acceptedTypes.push(FDS_ITEM_DRAG_TYPE);
	}

	const [{isOverCurrent}, dropRef] = useDrop({
		accept: acceptedTypes,
		canDrop(draggedItem: any, monitor) {
			if (monitor.getItemType() === FDS_ITEM_DRAG_TYPE) {
				if (
					item &&
					draggedItem &&
					draggedItem.embedded?.id === item.embedded?.id &&
					draggedItem.entryClassName === item.entryClassName
				) {
					return false;
				}

				return itemDropActive && isDropTarget(item);
			}

			return fileDropActive && isDropTarget(item);
		},
		collect: (monitor: DropTargetMonitor) => {
			return {
				isOverCurrent:
					dropActive &&
					monitor.canDrop() &&
					monitor.isOver({shallow: true}),
			};
		},
		drop(droppedItem: any, monitor) {
			if (monitor.isOver({shallow: true})) {
				if (targetDropRefQuerySelector && targetDropElementRef) {
					targetDropElementRef.current?.classList.remove(
						dropTargetClass
					);
				}

				if (monitor.getItemType() === FDS_ITEM_DRAG_TYPE) {
					handleItemDrop?.(droppedItem, item);
				}
				else {
					handleFileDrop?.(droppedItem, item);
				}
			}
		},
	});

	useEffect(() => {
		if (
			targetDropRef &&
			targetDropRef.current &&
			isDropTarget(item) &&
			dropActive
		) {
			dropRef(targetDropRef);

			if (targetDropRefQuerySelector) {
				targetDropElementRef.current =
					targetDropRef.current?.querySelector(
						targetDropRefQuerySelector
					);
			}
		}
	}, [
		isDropTarget,
		dropRef,
		item,
		dropActive,
		targetDropRef,
		targetDropRefQuerySelector,
	]);

	useEffect(() => {
		if (!targetDropRefQuerySelector) {
			return;
		}

		if (isOverCurrent) {
			targetDropElementRef?.current?.classList.add(dropTargetClass);
		}
		else {
			targetDropElementRef?.current?.classList.remove(dropTargetClass);
		}
	}, [isOverCurrent, targetDropRefQuerySelector]);

	return {
		className: isOverCurrent ? dropTargetClass : '',
		dropRef: isDropTarget(item) ? dropRef : nonDroppableRef,
		isOverCurrent,
	};
};

export default useFDSDrop;
